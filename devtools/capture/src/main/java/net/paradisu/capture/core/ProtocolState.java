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

/** Connection states. The numbers intentionally match ProtocolCraft (and therefore SniffCraft). */
public final class ProtocolState {
    public static final byte HANDSHAKE = 0;
    public static final byte STATUS = 1;
    public static final byte LOGIN = 2;
    public static final byte PLAY = 3;
    public static final byte CONFIGURATION = 4;
    /** Tracking was lost (unknown protocol, malformed data); packets are still recorded, tagged with this. */
    public static final byte UNKNOWN = (byte) 255;

    private ProtocolState() {}
}
