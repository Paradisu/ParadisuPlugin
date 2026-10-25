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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Fans formatted JSON lines out to live watchers. Each watcher has its own bounded queue; slow ones lose lines. */
public final class LiveHub {
    /** Which events a watcher wants. Null sets mean "everything". */
    public static final class Filter {
        public Set<Integer> conns;
        public Set<Integer> dirs;
        public Set<PacketKey> keys;
        public int payloadBytes;

        boolean accepts(int conn, int dir, int state, int id) {
            return (conns == null || conns.contains(conn))
                    && (dirs == null || dirs.contains(dir))
                    && (keys == null || keys.contains(new PacketKey(state, dir, id)));
        }
    }

    public static final class Subscriber {
        public final Filter filter;
        public final ArrayBlockingQueue<String> lines = new ArrayBlockingQueue<>(4096);
        public final AtomicLong dropped = new AtomicLong();

        Subscriber(Filter filter) {
            this.filter = filter;
        }

        void push(String line) {
            if (!lines.offer(line)) {
                dropped.incrementAndGet();
            }
        }
    }

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public boolean active() {
        return !subscribers.isEmpty();
    }

    public Subscriber subscribe(Filter filter) {
        Subscriber s = new Subscriber(filter);
        subscribers.add(s);
        return s;
    }

    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    public void packet(long seq, int conn, long micros, int dir, int state, byte[] frame) {
        int id = PacketIds.read(frame);
        for (Subscriber s : subscribers) {
            if (!s.filter.accepts(conn, dir, state, id)) {
                continue;
            }
            StringBuilder sb = new StringBuilder(96);
            sb.append("{\"t\":\"packet\",\"seq\":")
                    .append(seq)
                    .append(",\"conn\":")
                    .append(conn)
                    .append(",\"us\":")
                    .append(micros)
                    .append(",\"dir\":")
                    .append(dir)
                    .append(",\"state\":")
                    .append(state)
                    .append(",\"id\":")
                    .append(id)
                    .append(",\"size\":")
                    .append(frame.length);
            if (s.filter.payloadBytes > 0) {
                int from = PacketIds.width(frame);
                int to = Math.min(frame.length, from + s.filter.payloadBytes);
                sb.append(",\"hex\":\"");
                for (int i = from; i < to; i++) {
                    sb.append(Character.forDigit((frame[i] >> 4) & 0xF, 16))
                            .append(Character.forDigit(frame[i] & 0xF, 16));
                }
                sb.append('"');
            }
            sb.append('}');
            s.push(sb.toString());
        }
    }

    /** Pushes an already-built line (open/identify/close/stopped events) to every watcher. */
    public void event(String line) {
        for (Subscriber s : subscribers) {
            s.push(line);
        }
    }
}
