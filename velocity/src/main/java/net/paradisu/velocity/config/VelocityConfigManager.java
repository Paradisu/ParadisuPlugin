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
        this.configs.put("paradisu", new ConfigEntry<>(ParadisuConfig.class, dataDirectory.resolve("paradisu.yml"), "Paradisu Main Configuration"));
        this.configs.put("messages", new ConfigEntry<>(MessagesConfig.class, dataDirectory.resolve("messages.yml"), "Messages Configuration"));
    }
}
