/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.capture.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Platform-independent capture core: the hot-path entry points, session lifecycle and connection registry. */
public final class CaptureEngine {
    private final String platform;
    private final String pluginVersion;
    private final ProtocolHints hints;
    private final CaptureLog log;
    private final LiveHub hub = new LiveHub();
    private final AtomicInteger nextConnection = new AtomicInteger(1);
    private final Map<Integer, ConnectionInfo> connections = new ConcurrentHashMap<>();
    private volatile Session active;
    private volatile WriterThread writer;
    private volatile boolean warnedHints;

    public CaptureEngine(String platform, String pluginVersion, ProtocolHints hints, CaptureLog log) {
        this.platform = platform;
        this.pluginVersion = pluginVersion;
        this.hints = hints;
        this.log = log;
    }

    public String platform() {
        return platform;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public ProtocolHints hints() {
        return hints;
    }

    public LiveHub hub() {
        return hub;
    }

    public CaptureLog log() {
        return log;
    }

    public Session active() {
        return active;
    }

    public Iterable<ConnectionInfo> connections() {
        return connections.values();
    }

    public ConnectionInfo newConnection(Leg leg, String remote) {
        ConnectionInfo c = new ConnectionInfo(nextConnection.getAndIncrement(), leg, remote);
        connections.put(c.id, c);
        return c;
    }

    /** Hot path: one decoded frame (after decryption/decompression), in either direction. */
    public void onFrame(ConnectionInfo c, Direction d, ByteReader frame) {
        if (c.ignored) {
            return;
        }
        byte stateBefore = c.state;
        boolean wasIdentified = c.identified;
        boolean identifiedNow = false;
        if (d == Direction.SERVERBOUND && stateBefore != ProtocolState.UNKNOWN) {
            identifiedNow = ProtocolSniffer.observe(c, d, frame, hints);
            if (identifiedNow && c.state == ProtocolState.UNKNOWN && !warnedHints) {
                warnedHints = true;
                log.warn("client protocol " + c.protocol + " does not match the capture hints (" + hints.protocol
                        + "); packet states after login are not tracked");
            }
        }
        if (c.ignored) {
            return;
        }
        long now = System.nanoTime();
        // Handshake and hello are tiny; keeping them lets a recording that starts later still begin at login
        boolean stashed = false;
        if (!wasIdentified && d == Direction.SERVERBOUND && c.preLoginCount < c.preLogin.length) {
            c.preLogin[c.preLoginCount++] = new ConnectionInfo.PreFrame(now, stateBefore & 0xFF, frame.copy());
            stashed = true;
        }
        Session s = active;
        if (s == null) {
            return;
        }
        boolean boundBefore = c.bound == s;
        if (!boundBefore && c.rejectedFor != s) {
            tryBind(c, s, identifiedNow, stashed, now);
        }
        if (c.bound == s) {
            if (identifiedNow && boundBefore) {
                // Bound at the handshake (no player filter); the name only becomes known with the login hello
                s.offer(new Event.Identify(c.id, c.name, c.uuid));
            }
            s.offer(new Event.Packet(c.id, now, d.code(), stateBefore & 0xFF, frame.copy()));
        }
    }

    private void tryBind(ConnectionInfo c, Session s, boolean identifiedNow, boolean currentStashed, long now) {
        SessionSpec spec = s.spec;
        boolean needsIdentity = s.filtersPlayers() || spec.nextJoin;
        if (needsIdentity) {
            if (!c.identified) {
                return;
            }
            if (spec.nextJoin && !identifiedNow) {
                c.rejectedFor = s;
                return;
            }
            if (!s.matchesPlayer(c)) {
                c.rejectedFor = s;
                return;
            }
        } else if (c.protocol < 0) {
            return;
        }
        if (spec.nextJoin && !s.bound.compareAndSet(false, true)) {
            c.rejectedFor = s;
            return;
        }
        c.bound = s;
        s.offer(new Event.Open(c.id, c.leg.code(), c.remote, c.protocol, c.nextState, now));
        if (c.identified) {
            s.offer(new Event.Identify(c.id, c.name, c.uuid));
        }
        // Replay the stashed login frames; the caller records the frame being processed itself
        int replay = currentStashed ? c.preLoginCount - 1 : c.preLoginCount;
        for (int i = 0; i < replay; i++) {
            ConnectionInfo.PreFrame f = c.preLogin[i];
            s.offer(new Event.Packet(c.id, f.nanos(), Direction.SERVERBOUND.code(), f.state(), f.frame()));
        }
    }

    public void onClose(ConnectionInfo c) {
        connections.remove(c.id);
        Session s = active;
        if (s != null && c.bound == s) {
            s.offer(new Event.Close(c.id, System.nanoTime()));
        }
        c.bound = null;
    }

    /**
     * Starts a session. Returns an error message, or null on success. A live-only session (nothing recorded, usually
     * started by a watcher) yields to the new one; its watchers see it stop with reason "replaced".
     */
    public String start(SessionSpec spec) {
        String result = null;
        // A watcher may start a new live session in the gap, so replacing is retried a few times
        for (int attempt = 0; attempt < 3; attempt++) {
            Session current = active;
            if (current != null && current.spec.outputDir == null) {
                stopAndWait("replaced");
            }
            result = startNow(spec);
            Session now = active;
            if (result == null || now == null || now.spec.outputDir != null) {
                return result;
            }
        }
        return result;
    }

    private synchronized String startNow(SessionSpec spec) {
        if (active != null) {
            return "a capture is already running on this instance (" + active.spec.id + ")";
        }
        Session session = new Session(spec);
        try {
            WriterThread thread = new WriterThread(this, session, hub, log);
            if (spec.nextJoin) {
                for (ConnectionInfo c : connections.values()) {
                    c.rejectedFor = session;
                }
            }
            writer = thread;
            active = session;
            thread.start();
        } catch (IOException | RuntimeException e) {
            return "could not start the capture: " + e;
        }
        return null;
    }

    /** Stops the active session; the writer drains and finishes asynchronously. Returns the session, or null. */
    public synchronized Session stop(String reason) {
        Session s = active;
        if (s == null) {
            return null;
        }
        active = null;
        s.requestStop(reason);
        return s;
    }

    /** Stops and waits briefly for the dump to be finalised. */
    public Session stopAndWait(String reason) {
        WriterThread thread = writer;
        Session s = stop(reason);
        if (s != null && thread != null) {
            thread.await(5000);
        }
        return s;
    }

    public void shutdown() {
        stopAndWait("shutdown");
    }

    void writerFinished(Session session) {
        synchronized (this) {
            if (active == session) {
                active = null;
            }
        }
    }

    public List<ConnectionInfo> snapshotConnections() {
        return new ArrayList<>(connections.values());
    }
}
