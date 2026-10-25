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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Passes bytes through unchanged while copying them to a {@link FrameSplitter}. */
final class TapInputStream extends FilterInputStream {
    private final FrameSplitter splitter;
    private final Runnable onClose;
    private final byte[] one = new byte[1];
    private boolean closed;

    TapInputStream(InputStream in, FrameSplitter splitter, Runnable onClose) {
        super(in);
        this.splitter = splitter;
        this.onClose = onClose;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b >= 0) {
            one[0] = (byte) b;
            splitter.accept(one, 0, 1);
        } else {
            closed();
        }
        return b;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int n;
        try {
            n = in.read(buffer, offset, length);
        } catch (IOException e) {
            closed();
            throw e;
        }
        if (n > 0) {
            splitter.accept(buffer, offset, n);
        } else if (n < 0) {
            closed();
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        closed();
        super.close();
    }

    private void closed() {
        if (!closed) {
            closed = true;
            onClose.run();
        }
    }
}
