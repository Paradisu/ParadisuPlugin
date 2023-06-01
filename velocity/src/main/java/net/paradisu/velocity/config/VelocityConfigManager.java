package net.paradisu.velocity.config;

import net.paradisu.velocity.ParadisuVelocity;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.velocity.config.configs.ParadisuConfig;
import net.paradisu.core.config.ConfigManager;
import net.paradisu.core.config.loader.ConfigEntry;
import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class VelocityConfigManager implements ConfigManager {
    private final Map<String, ConfigEntry<?>> configs = new HashMap<>();

    public VelocityConfigManager(ParadisuVelocity paradisu) {
        Path dataDirectory = paradisu.dataDirectory();
        this.configs.put("paradisu", new ConfigEntry<>(ParadisuConfig.class, dataDirectory.resolve("paradisu.yml"), "Paradisu Main Configuration"));
        this.configs.put("messages", new ConfigEntry<>(MessagesConfig.class, dataDirectory.resolve("messages.yml"), "Messages Configuration"));
    }

    @Override
    public void loadConfigs() {
        try {
            for (ConfigEntry<?> config : this.configs.values()) {
                config.loadConfig();
            }
        } catch (ConfigurateException e) {
            throw new IllegalStateException("Failed to load config", e);
        }
    }

    @Override
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
