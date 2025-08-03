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

package net.paradisu.velocity.config;

import net.paradisu.core.config.ConfigManager;
import net.paradisu.core.config.loader.ConfigEntry;
import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.velocity.config.configs.ParadisuConfig;

import java.nio.file.Path;

public final class VelocityConfigManager extends ConfigManager {
    public VelocityConfigManager(ParadisuVelocity paradisu) {
        super(paradisu);
        Path dataDirectory = paradisu.dataDirectory();
        this.configs.put(
                "paradisu",
                new ConfigEntry<>(
                        ParadisuConfig.class, dataDirectory.resolve("paradisu.yml"), "Paradisu Main Configuration"));
        this.configs.put(
                "messages",
                new ConfigEntry<>(
                        MessagesConfig.class, dataDirectory.resolve("messages.yml"), "Messages Configuration"));
    }
}
