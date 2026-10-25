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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Writes the dump stream: a gzip of {@code "PDCAP" u8 format varlong startEpochMs} then records.
 *
 * <p>Format rule: adding a record type or changing a record body requires bumping {@link #FORMAT} here and in
 * {@code dev/src/capture/format.ts}; readers must keep reading older formats.
 */
public final class DumpWriter implements Closeable {
    public static final int FORMAT = 1;
    public static final int OPEN = 1;
    public static final int IDENTIFY = 2;
    public static final int PACKET = 3;
    public static final int CLOSE = 4;
    public static final int NOTE = 5;

    private final OutputStream out;
    private final byte[] scratch = new byte[10];

    public DumpWriter(Path file, int level, long startEpochMs) throws IOException {
        this.out =
                new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(file.toFile()), 1 << 16), 1 << 16, true) {
                    {
                        def.setLevel(Math.max(1, Math.min(9, level)));
                    }
                };
        out.write(new byte[] {'P', 'D', 'C', 'A', 'P', (byte) FORMAT});
        varLong(startEpochMs);
    }

    public void open(int conn, int leg, String remote, int protocol, int nextState) throws IOException {
        out.write(OPEN);
        varInt(conn);
        out.write(leg);
        string(remote);
        varInt(protocol + 1);
        varInt(nextState + 1);
    }

    public void identify(int conn, String name, UUID uuid) throws IOException {
        out.write(IDENTIFY);
        varInt(conn);
        string(name == null ? "" : name);
        out.write(uuid == null ? 0 : 1);
        if (uuid != null) {
            long msb = uuid.getMostSignificantBits();
            long lsb = uuid.getLeastSignificantBits();
            for (int i = 7; i >= 0; i--) {
                out.write((int) (msb >>> (8 * i)));
            }
            for (int i = 7; i >= 0; i--) {
                out.write((int) (lsb >>> (8 * i)));
            }
        }
    }

    public void packet(int conn, long micros, int dir, int state, byte[] frame) throws IOException {
        out.write(PACKET);
        varInt(conn);
        varLong(micros);
        out.write(dir);
        out.write(state);
        varInt(frame.length);
        out.write(frame);
    }

    public void close(int conn, long micros) throws IOException {
        out.write(CLOSE);
        varInt(conn);
        varLong(micros);
    }

    public void note(String json) throws IOException {
        out.write(NOTE);
        string(json);
    }

    /** Makes everything written so far readable (sync flush), so a crash leaves a usable prefix. */
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private void varInt(int value) throws IOException {
        int n = 0;
        while ((value & ~0x7F) != 0) {
            scratch[n++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        scratch[n++] = (byte) value;
        out.write(scratch, 0, n);
    }

    private void varLong(long value) throws IOException {
        int n = 0;
        while ((value & ~0x7FL) != 0) {
            scratch[n++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        scratch[n++] = (byte) value;
        out.write(scratch, 0, n);
    }

    private void string(String value) throws IOException {
        byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
        varInt(bytes.length);
        out.write(bytes);
    }
}
