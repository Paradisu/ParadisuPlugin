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

package net.paradisu.capture.netty;

import io.netty.buffer.ByteBuf;
import net.paradisu.capture.core.ByteReader;

/** Reads a netty buffer without moving its indexes or touching its reference count. */
final class ByteBufReader implements ByteReader {
    private final ByteBuf buf;
    private final int base;

    ByteBufReader(ByteBuf buf) {
        this.buf = buf;
        this.base = buf.readerIndex();
    }

    @Override
    public int length() {
        return buf.writerIndex() - base;
    }

    @Override
    public byte get(int index) {
        return buf.getByte(base + index);
    }

    @Override
    public byte[] copy() {
        byte[] bytes = new byte[length()];
        buf.getBytes(base, bytes);
        return bytes;
    }
}
