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

/** Written next to the dump as {@code <instance>.json}. Plain classes for Gson. */
public final class Manifest {
    public int format = 1;
    public String id;
    public String instance;
    public String platform;
    public String pluginVersion;
    public String startedAt;
    public String stoppedAt;
    public String stopReason;
    public SessionSpec spec;
    public long packets;
    public long bytes;
    public long dropped;
    public List<Conn> connections = new ArrayList<>();
    public List<String> warnings = new ArrayList<>();

    public static final class Conn {
        public int id;
        public int leg;
        public String remote;
        public int protocol;
        public String name;
        public String uuid;
    }
}
