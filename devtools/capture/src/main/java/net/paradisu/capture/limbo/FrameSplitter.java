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

import net.paradisu.capture.core.ArrayReader;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.Direction;

import java.util.Arrays;

/** Incremental {@code [varint length][body]} parser feeding complete frames to the engine. One thread per instance. */
final class FrameSplitter {
    private final CaptureEngine engine;
    private final ConnectionInfo info;
    private final Direction direction;
    private byte[] body = new byte[256];
    private int length;
    private int lengthShift;
    private int bodyRead;
    private boolean readingBody;
    private boolean broken;

    FrameSplitter(CaptureEngine engine, ConnectionInfo info, Direction direction) {
        this.engine = engine;
        this.info = info;
        this.direction = direction;
    }

    void accept(byte[] data, int offset, int count) {
        if (broken) {
            return;
        }
        try {
            int i = offset;
            int end = offset + count;
            while (i < end) {
                if (!readingBody) {
                    byte b = data[i++];
                    length |= (b & 0x7F) << lengthShift;
                    if ((b & 0x80) != 0) {
                        lengthShift += 7;
                        if (lengthShift > 28) {
                            broken = true;
                            return;
                        }
                        continue;
                    }
                    if (length < 0 || length > (1 << 24)) {
                        broken = true;
                        return;
                    }
                    readingBody = true;
                    bodyRead = 0;
                    if (body.length < length) {
                        body = Arrays.copyOf(body, Math.max(length, body.length * 2));
                    }
                    if (length == 0) {
                        finishFrame();
                    }
                } else {
                    int take = Math.min(length - bodyRead, end - i);
                    System.arraycopy(data, i, body, bodyRead, take);
                    bodyRead += take;
                    i += take;
                    if (bodyRead == length) {
                        finishFrame();
                    }
                }
            }
        } catch (Throwable t) {
            broken = true;
            info.ignored = true;
            engine.log().warn("capture disabled for Limbo connection " + info.id + ": " + t);
        }
    }

    private void finishFrame() {
        if (length > 0) {
            engine.onFrame(info, direction, new ArrayReader(body, length));
        }
        readingBody = false;
        length = 0;
        lengthShift = 0;
    }
}
