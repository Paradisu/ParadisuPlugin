package net.paradisu.core.config.loader;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

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
                    "Failed to initialize an object mapper for type: " + configClass.getSimpleName(),
                    e
            );
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
