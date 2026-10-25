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
 * Per-connection state. Written only from that connection's own thread (event loop or Limbo connection thread); other
 * threads only read the volatile fields.
 */
public final class ConnectionInfo {
    public final int id;
    public final Leg leg;
    public volatile String remote;

    public volatile int protocol = -1;
    public volatile int nextState = -1;
    public volatile String name;
    public volatile UUID uuid;
    public volatile byte state = ProtocolState.HANDSHAKE;
    public volatile boolean ignored;
    /** The session this connection is recorded in (recording means {@code bound == engine.active}). */
    public volatile Session bound;
    /** A session this connection was already evaluated against and does not belong in. */
    public volatile Session rejectedFor;
    /** Handler names of the live pipeline, for diagnostics; null when the platform has none. */
    public volatile java.util.function.Supplier<java.util.List<String>> pipelineNames;
    /** Whether the login hello was seen (the connection is identified). */
    public volatile boolean identified;
    /** Serverbound handshake and hello frames, kept so a recording that starts at login can include them. */
    public final PreFrame[] preLogin = new PreFrame[4];

    public int preLoginCount;

    /** A frame seen before the connection was identified. */
    public record PreFrame(long nanos, int state, byte[] frame) {}

    public ConnectionInfo(int id, Leg leg, String remote) {
        this.id = id;
        this.leg = leg;
        this.remote = remote;
    }
}
