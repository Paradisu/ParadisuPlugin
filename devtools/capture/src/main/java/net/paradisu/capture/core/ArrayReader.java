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

import java.util.Arrays;

/** {@link ByteReader} over a prefix of an array (not copied until {@link #copy()}). */
public final class ArrayReader implements ByteReader {
    private final byte[] data;
    private final int length;

    public ArrayReader(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte get(int index) {
        return data[index];
    }

    @Override
    public byte[] copy() {
        return Arrays.copyOf(data, length);
    }
}
