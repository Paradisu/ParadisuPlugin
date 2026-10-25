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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** One capture run on one instance. */
public final class Session {
    public final SessionSpec spec;
    public final long startNanos = System.nanoTime();
    public final long startEpochMs = System.currentTimeMillis();
    public final AtomicLong packets = new AtomicLong();
    public final AtomicLong bytes = new AtomicLong();
    public final AtomicLong dropped = new AtomicLong();
    /** For next-join sessions: set once a connection has been bound. */
    public final AtomicBoolean bound = new AtomicBoolean();

    public final ArrayBlockingQueue<Event> queue;
    public volatile String stopReason;
    public volatile boolean closing;
    private final Set<String> players = new HashSet<>();

    public Session(SessionSpec spec) {
        this.spec = spec;
        this.queue = new ArrayBlockingQueue<>(Math.max(1024, spec.queueCapacity));
        for (String player : spec.players) {
            players.add(normalize(player));
        }
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace("-", "");
    }

    public boolean filtersPlayers() {
        return !players.isEmpty();
    }

    public boolean matchesPlayer(ConnectionInfo c) {
        if (players.isEmpty()) {
            return true;
        }
        String name = c.name;
        if (name != null && players.contains(normalize(name))) {
            return true;
        }
        return c.uuid != null && players.contains(normalize(c.uuid.toString()));
    }

    /** Never blocks: a full queue drops the event and counts it. */
    public void offer(Event event) {
        if (!queue.offer(event)) {
            dropped.incrementAndGet();
        }
    }

    public void requestStop(String reason) {
        if (stopReason == null) {
            stopReason = reason;
        }
        closing = true;
    }
}
