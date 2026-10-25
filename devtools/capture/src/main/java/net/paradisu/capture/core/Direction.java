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

/** Which way a packet travels, relative to the Minecraft server side of the link. */
public enum Direction {
    SERVERBOUND(0),
    CLIENTBOUND(1);

    private final int code;

    Direction(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public Direction opposite() {
        return this == SERVERBOUND ? CLIENTBOUND : SERVERBOUND;
    }

    public static Direction of(int code) {
        return code == 0 ? SERVERBOUND : CLIENTBOUND;
    }
}
