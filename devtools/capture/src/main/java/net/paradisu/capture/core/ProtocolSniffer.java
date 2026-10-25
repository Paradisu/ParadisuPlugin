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

import java.util.UUID;

/**
 * Follows a connection's state from its serverbound frames only. Allocation-free except for the player name, and it
 * never throws: anything unexpected sets the state to {@link ProtocolState#UNKNOWN}.
 */
public final class ProtocolSniffer {
    private ProtocolSniffer() {}

    /** Result of reading a varint: value and the offset after it, or -1 offset when malformed. */
    private static final class Cursor {
        final ByteReader r;
        int pos;

        Cursor(ByteReader r) {
            this.r = r;
        }

        int varInt() {
            int value = 0;
            for (int i = 0; i < 5; i++) {
                if (pos >= r.length()) {
                    throw new IllegalStateException();
                }
                byte b = r.get(pos++);
                value |= (b & 0x7F) << (7 * i);
                if ((b & 0x80) == 0) {
                    return value;
                }
            }
            throw new IllegalStateException();
        }

        void skipString() {
            int len = varInt();
            if (len < 0 || pos + len > r.length()) {
                throw new IllegalStateException();
            }
            pos += len;
        }

        String string(int maxBytes) {
            int len = varInt();
            if (len < 0 || len > maxBytes || pos + len > r.length()) {
                throw new IllegalStateException();
            }
            byte[] bytes = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = r.get(pos + i);
            }
            pos += len;
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }

        int unsignedShort() {
            if (pos + 2 > r.length()) {
                throw new IllegalStateException();
            }
            int v = ((r.get(pos) & 0xFF) << 8) | (r.get(pos + 1) & 0xFF);
            pos += 2;
            return v;
        }

        long long64() {
            if (pos + 8 > r.length()) {
                throw new IllegalStateException();
            }
            long v = 0;
            for (int i = 0; i < 8; i++) {
                v = (v << 8) | (r.get(pos + i) & 0xFFL);
            }
            pos += 8;
            return v;
        }

        int remaining() {
            return r.length() - pos;
        }
    }

    /**
     * Updates {@code c} from one frame.
     *
     * @return true when this frame identified the connection (the login hello), so the caller can bind sessions
     */
    public static boolean observe(ConnectionInfo c, Direction d, ByteReader frame, ProtocolHints hints) {
        if (c.ignored || d != Direction.SERVERBOUND || c.state == ProtocolState.UNKNOWN) {
            return false;
        }
        try {
            Cursor cur = new Cursor(frame);
            if (c.state == ProtocolState.HANDSHAKE && frame.length() > 0 && (frame.get(0) & 0xFF) == 0xFE) {
                // Legacy server list ping: not a framed connection
                c.ignored = true;
                return false;
            }
            int id = cur.varInt();
            switch (c.state) {
                case ProtocolState.HANDSHAKE -> {
                    if (id != 0) {
                        c.ignored = true;
                        return false;
                    }
                    c.protocol = cur.varInt();
                    cur.skipString();
                    cur.unsignedShort();
                    int next = cur.varInt();
                    c.nextState = next;
                    if (next == 2 || next == 3) {
                        c.state = ProtocolState.LOGIN;
                    } else if (next == 1) {
                        c.state = ProtocolState.STATUS;
                        c.ignored = true;
                    } else {
                        c.ignored = true;
                    }
                }
                case ProtocolState.LOGIN -> {
                    if (id == 0 && !c.identified) {
                        c.name = cur.string(64);
                        if (cur.remaining() >= 16) {
                            long msb = cur.long64();
                            long lsb = cur.long64();
                            c.uuid = new UUID(msb, lsb);
                        }
                        c.identified = true;
                        if (!hints.complete() || hints.protocol != c.protocol) {
                            // Cannot follow transitions for this protocol; keep recording with an unknown state
                            c.state = ProtocolState.UNKNOWN;
                        }
                        return true;
                    }
                    if (hints.complete() && id == hints.loginAcknowledged) {
                        c.state = ProtocolState.CONFIGURATION;
                    }
                }
                case ProtocolState.CONFIGURATION -> {
                    if (id == hints.finishConfiguration) {
                        c.state = ProtocolState.PLAY;
                    }
                }
                case ProtocolState.PLAY -> {
                    if (id == hints.configurationAcknowledged) {
                        c.state = ProtocolState.CONFIGURATION;
                    }
                }
                default -> {}
            }
        } catch (RuntimeException e) {
            c.state = ProtocolState.UNKNOWN;
        }
        return false;
    }
}
