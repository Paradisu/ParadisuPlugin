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

package net.paradisu.capture.limbo;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Passes bytes through unchanged while copying them to a {@link FrameSplitter}. */
final class TapOutputStream extends FilterOutputStream {
    private final FrameSplitter splitter;
    private final byte[] one = new byte[1];

    TapOutputStream(OutputStream out, FrameSplitter splitter) {
        super(out);
        this.splitter = splitter;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        one[0] = (byte) b;
        splitter.accept(one, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        out.write(buffer, offset, length);
        splitter.accept(buffer, offset, length);
    }
}
