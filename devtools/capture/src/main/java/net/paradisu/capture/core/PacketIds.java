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

/** Helpers for the leading packet id varint of a frame. */
public final class PacketIds {
    private PacketIds() {}

    /** The packet id, or -1 when the frame does not start with a valid varint. */
    public static int read(byte[] frame) {
        int value = 0;
        for (int i = 0; i < 5 && i < frame.length; i++) {
            value |= (frame[i] & 0x7F) << (7 * i);
            if ((frame[i] & 0x80) == 0) {
                return value;
            }
        }
        return -1;
    }

    /** Number of bytes the leading varint takes, or 0 when malformed. */
    public static int width(byte[] frame) {
        for (int i = 0; i < 5 && i < frame.length; i++) {
            if ((frame[i] & 0x80) == 0) {
                return i + 1;
            }
        }
        return 0;
    }
}
