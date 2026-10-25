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

/** What the hot path hands to the writer thread. */
public sealed interface Event {
    record Open(int conn, int leg, String remote, int protocol, int nextState, long nanos) implements Event {}

    record Identify(int conn, String name, UUID uuid) implements Event {}

    record Packet(int conn, long nanos, int dir, int state, byte[] frame) implements Event {}

    record Close(int conn, long nanos) implements Event {}
}
