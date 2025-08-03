/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
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

package net.paradisu.paper.config.configs;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Accessors(fluent = true)
@Getter
@ConfigSerializable
public final class ParadisuConfig {
    private Context context = new Context();
    private Database database = new Database();

    @Getter
    @ConfigSerializable
    public static final class Database {
        @Setting("url")
        private String url = "";

        @Setting("username")
        private String username = "";

        @Setting("password")
        private String password = "";
    }

    @Getter
    @ConfigSerializable
    public static final class Context {
        @Setting("server")
        private String server = "paradisu";

        @Setting("warp")
        private String warp = "paradisu";

        @Setting("inventory")
        private String inventory = "paradisu";
    }
}
