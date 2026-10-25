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

import java.util.ArrayList;
import java.util.List;

/** What to capture and when to stop. A plain class (public fields, no-arg constructor) so old Gson versions read it. */
public final class SessionSpec {
    public String id;
    public String instance;
    /** Directory for the dump and manifest; null records nothing (live watching only). */
    public String outputDir;
    /** Player names (case-insensitive) or UUIDs; empty means every player. */
    public List<String> players = new ArrayList<>();
    /** Record only the next connection that logs in and matches the player filter. */
    public boolean nextJoin;

    public long maxPackets;
    public long maxBytes;
    public long durationMs;
    /** Stop after a packet of one of these types has been recorded. */
    public List<PacketKey> until = new ArrayList<>();

    public int gzipLevel = 3;
    public int queueCapacity = 65536;
}
