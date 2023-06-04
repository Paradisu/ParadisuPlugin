package net.paradisu.paper.config;

import net.paradisu.core.config.ConfigManager;
import net.paradisu.core.config.loader.ConfigEntry;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.config.configs.MessagesConfig;

import java.nio.file.Path;

public final class PaperConfigManager extends ConfigManager {
    public PaperConfigManager(ParadisuPaper paradisu) {
        super(paradisu);
        Path dataDirectory = paradisu.dataDirectory();
        this.configs.put("messages", new ConfigEntry<>(MessagesConfig.class, dataDirectory.resolve("messages.yml"), "Messages Configuration"));
    }
}
