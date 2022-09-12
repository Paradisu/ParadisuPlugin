package net.paradisu.velocity.config;

import net.paradisu.velocity.Paradisu;
import net.paradisu.velocity.config.loader.ConfigLoader;
import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;

public final class ConfigManager {
    private final ConfigLoader<ParadisuConfig> paradisuConfigLoader;
    private final ConfigLoader<MessagesConfig> messagesConfigLoader;

    private ParadisuConfig paradisuConfig;
    private MessagesConfig messagesConfig;

    public ConfigManager(Paradisu paradisu) {
        Path dataDirectory = paradisu.dataDirectory();
        this.paradisuConfigLoader = new ConfigLoader<>(
    ParadisuConfig.class,
                dataDirectory.resolve("paradisu.yml"),
                options -> options.header("Paradisu Main Configuration")
        );

        this.messagesConfigLoader = new ConfigLoader<>(
    MessagesConfig.class,
                dataDirectory.resolve("messages.yml"),
                options -> options.header("Messages Configuration")
        );
    }

    public void loadConfigs() {
        try {
            this.paradisuConfig = this.paradisuConfigLoader.load();
            this.paradisuConfigLoader.save(this.paradisuConfig);

            this.messagesConfig = this.messagesConfigLoader.load();
            this.messagesConfigLoader.save(this.messagesConfig);
        } catch (ConfigurateException e) {
            throw new IllegalStateException("Failed to load config", e);
        }
    }

    public ParadisuConfig paradisuConfig() {
        if (this.paradisuConfig == null) {
            throw new IllegalStateException("Config has not yet been loaded");
        }
        return this.paradisuConfig;
    }

    public MessagesConfig messagesConfig() {
        if (this.messagesConfig == null) {
            throw new IllegalStateException("Config has not yet been loaded");
        }
        return this.messagesConfig;
    }
}
