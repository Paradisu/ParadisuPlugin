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

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public final class ConfigLoader<C> {

    private final YamlConfigurationLoader loader;
    private final ObjectMapper<C> mapper;

    public ConfigLoader(Class<C> configClass, Path configPath, UnaryOperator<ConfigurationOptions> optionsModifier) {
        this.loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .defaultOptions(optionsModifier)
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        try {
            this.mapper = ObjectMapper.factory().get(configClass);
        } catch (SerializationException e) {
            throw new IllegalStateException(
                    "Failed to initialize an object mapper for type: " + configClass.getSimpleName(), e);
        }
    }

    public C load() throws ConfigurateException {
        CommentedConfigurationNode node = this.loader.load();
        return this.mapper.load(node);
    }

    public void save(C config) throws ConfigurateException {
        CommentedConfigurationNode node = this.loader.createNode();
        this.mapper.save(config, node);
        this.loader.save(node);
    }
}
