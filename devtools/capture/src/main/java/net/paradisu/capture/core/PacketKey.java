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

/** Identifies a packet type: connection state, direction code and packet id. Plain class for Gson. */
public final class PacketKey {
    public int state;
    public int dir;
    public int id;

    public PacketKey() {}

    public PacketKey(int state, int dir, int id) {
        this.state = state;
        this.dir = dir;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PacketKey k && k.state == state && k.dir == dir && k.id == id;
    }

    @Override
    public int hashCode() {
        return (state * 31 + dir) * 31 + id;
    }
}
