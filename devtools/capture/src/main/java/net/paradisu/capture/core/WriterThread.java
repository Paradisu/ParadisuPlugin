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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Drains a session's queue off the server threads: numbers packets, writes the dump, feeds live watchers and checks the
 * stop conditions.
 */
final class WriterThread extends Thread {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final CaptureEngine engine;
    private final Session session;
    private final LiveHub hub;
    private final CaptureLog log;
    private final Path manifestPath;
    private final Manifest manifest = new Manifest();
    private final Map<Integer, Manifest.Conn> conns = new LinkedHashMap<>();
    private final Set<PacketKey> until = new HashSet<>();
    /** Frontend connections that logged in as a player and have not closed yet (for stopOnDisconnect). */
    private final Set<Integer> players = new HashSet<>();

    private DumpWriter dump;
    private long seq;
    private long firstOpenNanos = -1;
    private long lastFlush = System.nanoTime();
    private boolean stopTriggered;

    WriterThread(CaptureEngine engine, Session session, LiveHub hub, CaptureLog log) throws IOException {
        super("ParadisuCapture-writer");
        setDaemon(true);
        this.engine = engine;
        this.session = session;
        this.hub = hub;
        this.log = log;
        SessionSpec spec = session.spec;
        until.addAll(spec.until);
        if (spec.outputDir != null) {
            Path dir = Path.of(spec.outputDir);
            Files.createDirectories(dir);
            String name = spec.instance == null ? "capture" : spec.instance;
            dump = new DumpWriter(dir.resolve(name + ".bin.gz"), spec.gzipLevel, session.startEpochMs);
            manifestPath = dir.resolve(name + ".json");
        } else {
            manifestPath = null;
        }
        manifest.id = spec.id;
        manifest.instance = spec.instance;
        manifest.platform = engine.platform();
        manifest.pluginVersion = engine.pluginVersion();
        manifest.startedAt = Instant.ofEpochMilli(session.startEpochMs).toString();
        manifest.spec = spec;
        if (!engine.hints().complete()) {
            manifest.warnings.add(
                    "protocol hints missing or incomplete: connection states are not tracked after login");
        }
        writeManifest();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Event event = session.queue.poll(250, TimeUnit.MILLISECONDS);
                if (event == null) {
                    checkDeadline();
                    flushIfDue();
                    if (session.closing) {
                        break;
                    }
                    continue;
                }
                handle(event);
                checkDeadline();
                flushIfDue();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            log.warn("capture writer failed: " + e);
            session.requestStop("error");
            engine.stop("error");
        } finally {
            finish();
        }
    }

    private void handle(Event event) throws IOException {
        switch (event) {
            case Event.Open o -> {
                if (firstOpenNanos < 0) {
                    firstOpenNanos = o.nanos();
                }
                Manifest.Conn c = new Manifest.Conn();
                c.id = o.conn();
                c.leg = o.leg();
                c.remote = o.remote();
                c.protocol = o.protocol();
                conns.put(o.conn(), c);
                if (dump != null) {
                    dump.open(o.conn(), o.leg(), o.remote(), o.protocol(), o.nextState());
                }
                hub.event("{\"t\":\"open\",\"conn\":" + o.conn() + ",\"leg\":" + o.leg() + ",\"remote\":"
                        + Json.quote(o.remote()) + ",\"protocol\":" + o.protocol() + "}");
            }
            case Event.Identify i -> {
                Manifest.Conn c = conns.get(i.conn());
                if (c != null) {
                    c.name = i.name();
                    c.uuid = i.uuid() == null ? null : i.uuid().toString();
                    if (c.leg == Leg.FRONTEND.code()) {
                        players.add(i.conn());
                    }
                }
                if (dump != null) {
                    dump.identify(i.conn(), i.name(), i.uuid());
                }
                hub.event(
                        "{\"t\":\"identify\",\"conn\":" + i.conn() + ",\"name\":" + Json.quote(i.name()) + ",\"uuid\":"
                                + Json.quote(i.uuid() == null ? null : i.uuid().toString()) + "}");
            }
            case Event.Packet p -> {
                if (stopTriggered) {
                    return;
                }
                long micros = Math.max(0, (p.nanos() - session.startNanos) / 1000);
                if (dump != null) {
                    dump.packet(p.conn(), micros, p.dir(), p.state(), p.frame());
                }
                hub.packet(seq, p.conn(), micros, p.dir(), p.state(), p.frame());
                seq++;
                long packets = session.packets.incrementAndGet();
                long bytes = session.bytes.addAndGet(p.frame().length);
                SessionSpec spec = session.spec;
                if (spec.maxPackets > 0 && packets >= spec.maxPackets) {
                    trigger("packets");
                } else if (spec.maxBytes > 0 && bytes >= spec.maxBytes) {
                    trigger("bytes");
                } else if (!until.isEmpty()
                        && until.contains(new PacketKey(p.state(), p.dir(), PacketIds.read(p.frame())))) {
                    trigger("until");
                }
            }
            case Event.Close c -> {
                long micros = Math.max(0, (c.nanos() - session.startNanos) / 1000);
                if (dump != null) {
                    dump.close(c.conn(), micros);
                }
                hub.event("{\"t\":\"close\",\"conn\":" + c.conn() + "}");
                if (players.remove(c.conn()) && players.isEmpty() && session.spec.stopOnDisconnect) {
                    trigger("disconnect");
                }
            }
        }
    }

    private void trigger(String reason) {
        stopTriggered = true;
        engine.stop(reason);
    }

    private void checkDeadline() {
        long duration = session.spec.durationMs;
        if (duration <= 0 || stopTriggered || session.closing) {
            return;
        }
        long base = session.spec.nextJoin ? firstOpenNanos : session.startNanos;
        if (base >= 0 && System.nanoTime() - base > TimeUnit.MILLISECONDS.toNanos(duration)) {
            trigger("duration");
        }
    }

    private void flushIfDue() throws IOException {
        if (dump != null && System.nanoTime() - lastFlush > TimeUnit.SECONDS.toNanos(1)) {
            dump.flush();
            lastFlush = System.nanoTime();
        }
    }

    private void writeManifest() throws IOException {
        if (manifestPath == null) {
            return;
        }
        manifest.packets = session.packets.get();
        manifest.bytes = session.bytes.get();
        manifest.dropped = session.dropped.get();
        manifest.connections = new java.util.ArrayList<>(conns.values());
        Files.writeString(manifestPath, GSON.toJson(manifest));
    }

    private void finish() {
        try {
            if (session.dropped.get() > 0) {
                manifest.warnings.add(session.dropped.get() + " events dropped because the writer queue was full");
            }
            manifest.stoppedAt = Instant.now().toString();
            manifest.stopReason = session.stopReason == null ? "shutdown" : session.stopReason;
            if (dump != null) {
                dump.close();
            }
            writeManifest();
        } catch (IOException | RuntimeException e) {
            log.warn("could not finish capture: " + e);
        }
        hub.event("{\"t\":\"stopped\",\"reason\":" + Json.quote(session.stopReason) + ",\"packets\":"
                + session.packets.get() + ",\"bytes\":" + session.bytes.get() + ",\"dropped\":"
                + session.dropped.get() + "}");
        engine.writerFinished(session);
    }

    /** Waits for the thread to finish writing (used on shutdown). */
    void await(long millis) {
        try {
            join(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
