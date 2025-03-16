package net.paradisu.paper;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.paper.commands.PaperCommandRegistrar;
import net.paradisu.paper.config.PaperConfigManager;
import net.paradisu.paper.config.configs.MessagesConfig;
import net.paradisu.paper.util.PaperLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class ParadisuPaper extends JavaPlugin implements ParadisuPlugin {
    private PaperCommandManager<CommandSourceStack> commandManager;
    private PaperLogger logger;
    private BukkitConnectorPlugin connector;
    private boolean connectorEnabled;
    private PaperConfigManager configManager;
    private TranslationManager translationManager;

    @Override
    public void onLoad() {
        this.logger = new PaperLogger(getLogger());
    }

    @Override
    public void onEnable() {
        try {
            this.configManager = new PaperConfigManager(this);
            this.configManager.loadConfigs();

            this.translationManager = new TranslationManager(this);
            this.translationManager.reload();

            this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);
            PaperCommandRegistrar.registerCommands(this);

            this.connectorEnabled = Bukkit.getPluginManager().isPluginEnabled("ConnectorPlugin");
            if (this.connectorEnabled) {
                this.connector = (BukkitConnectorPlugin) Bukkit.getPluginManager().getPlugin("ConnectorPlugin");
            }
        } catch (Exception e) {
            this.logger.error("Failed to enable ParadisuPlugin", e);
        }
    }

    @Override
    public CommandManager<CommandSourceStack> commandManager() {
        return commandManager;
    }

    @Override
    public BukkitConnectorPlugin connector() {
        return connector;
    }

    @Override
    public Path dataDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    public TranslationManager translationManager() {
        return translationManager;
    }

    @Override
    public PaperConfigManager configManager() {
        return configManager;
    }

    public MessagesConfig messagesConfig() {
        return configManager.getConfig("messages", MessagesConfig.class);
    }

    @Override
    public PaperLogger logger() {
        return logger;
    }

    @Override
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded Paradisu Paper plugin");
    }
}
