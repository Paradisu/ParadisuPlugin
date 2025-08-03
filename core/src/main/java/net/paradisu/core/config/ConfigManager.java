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

package net.paradisu.core.config;

import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.config.loader.ConfigEntry;
import org.spongepowered.configurate.ConfigurateException;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigManager {
    protected Map<String, ConfigEntry<?>> configs = new HashMap<>();

    public ConfigManager(ParadisuPlugin paradisu) {}

    public void loadConfigs() {
        try {
            for (ConfigEntry<?> config : this.configs.values()) {
                config.loadConfig();
            }
        } catch (ConfigurateException e) {
            throw new IllegalStateException("Failed to load config", e);
        }
    }

    public <T> T getConfig(String name, Class<T> type) {
        ConfigEntry<?> entry = this.configs.get(name);
        if (entry == null) {
            throw new IllegalArgumentException("No config registered with name: " + name);
        }
        if (!type.isAssignableFrom(entry.getType())) {
            throw new IllegalArgumentException("The config with name " + name + " is not of type " + type.getName());
        }
        return type.cast(entry.getConfig());
    }
}
