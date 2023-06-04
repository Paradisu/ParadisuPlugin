package net.paradisu.paper;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.paper.commands.AbstractPaperCommand;
import net.paradisu.paper.config.PaperConfigManager;
import net.paradisu.paper.config.configs.MessagesConfig;
import net.paradisu.paper.util.PaperLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

public class ParadisuPaper extends JavaPlugin implements ParadisuPlugin {
    private CommandManager<CommandSender> commandManager;
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

            this.commandManager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
            registerCommands();

            this.connectorEnabled = Bukkit.getPluginManager().isPluginEnabled("ConnectorPlugin");
            if (this.connectorEnabled) {
                this.connector = (BukkitConnectorPlugin) Bukkit.getPluginManager().getPlugin("ConnectorPlugin");
            }
        } catch (Exception e) {
            this.logger.error("Failed to enable ParadisuPlugin", e);
        }
    }

    @Override
    public CommandManager<CommandSender> commandManager() {
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

    /**
     * Registers all commands for this plugin via reflection.
     * Each class contains cloud commands for a specific category.
     */
    private void registerCommands() {
        Reflections reflections = new Reflections("net.paradisu.paper.commands.command");
        Set<Class<? extends AbstractPaperCommand>> classes = reflections
                .getSubTypesOf(AbstractPaperCommand.class);
        for (Class<? extends AbstractPaperCommand> clazz : classes) {
            try {
                Constructor<? extends AbstractPaperCommand> constructor = clazz.getDeclaredConstructor(this.getClass());
                AbstractPaperCommand command = constructor.newInstance(this);
                command.register();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded Paradisu Paper plugin");
    }
}
