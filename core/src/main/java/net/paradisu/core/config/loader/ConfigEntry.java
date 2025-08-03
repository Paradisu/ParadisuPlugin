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

package net.paradisu.core.config.loader;

import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;

public class ConfigEntry<T> {
    private final ConfigLoader<T> configLoader;
    private T config;
    private final Class<T> type;

    public ConfigEntry(Class<T> configClass, Path path, String header) {
        this.configLoader = new ConfigLoader<>(configClass, path, options -> options.header(header));
        this.type = configClass;
    }

    public void loadConfig() throws ConfigurateException {
        this.config = this.configLoader.load();
        this.configLoader.save(this.config);
    }

    public T getConfig() {
        if (this.config == null) {
            throw new IllegalStateException("Config has not yet been loaded");
        }
        return this.config;
    }

    public Class<T> getType() {
        return type;
    }
}
