package net.paradisu.velocity;

import com.google.inject.Inject;
import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import de.themoep.connectorplugin.velocity.VelocityConnectorPlugin;
import net.paradisu.velocity.config.VelocityConfigManager;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.commands.command.*;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
@Plugin(
    id = "paradisuplugin", 
    name = "ParadisuPlugin",
    version = "${version}",
    description = "The core plugin for the Paradisu Velocity proxy",
    authors = {"_Kastle", "cyto"},
    url = "https://paradisu.net",
    dependencies = @Dependency(id = "connectorplugin")
    )
public final class ParadisuVelocity implements ParadisuPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private VelocityConfigManager configManager;
    private TranslationManager translationManager;
    private VelocityCommandManager<CommandSource> commandManager;
    private boolean connectorEnabled;
    private VelocityConnectorPlugin connector;
    //private final VelocityConnectorPlugin connectorPlugin;
    
    @Inject
    public ParadisuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize the config manager
        this.configManager = new VelocityConfigManager(this);
        this.configManager.loadConfigs();
        // Initialize the translation manager
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();

        // Initialize the command manager
        this.commandManager = new VelocityCommandManager<>(
            this.server.getPluginManager().ensurePluginContainer(this), 
            this.server, 
            CommandExecutionCoordinator.simpleCoordinator(), 
            Function.identity(), 
            Function.identity()
            );
        registerCommands();

        // Initialize the connector plugin
        Optional<PluginContainer> connectorPlugin = getServer().getPluginManager().getPlugin("connectorplugin");
        connectorEnabled = connectorPlugin.isPresent();
        // Debug logging: was the connector plugin found?
        logger().info("ConnectorPlugin found: " + connectorEnabled);
        if (connectorEnabled) {
            this.connector = (VelocityConnectorPlugin) connectorPlugin.get().getInstance().get();
            // Debug logging: what is the connector plugin bridge?
            logger().info("ConnectorPlugin instance: " + this.connector.getBridge().toString());
            logger().info("ConnectorPlugin server: " + this.connector.getServerName());
        }
    }

    /**
     * Returns the logger for this plugin.
     * @return the logger for this plugin
     */
    public Logger logger() {
        return this.logger;
    }

    /**
     * Returns the data directory for this plugin.
     * @return the data directory for this plugin
     */
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    /**
     * Returns the Velocity Command Manager for this plugin.
     * @return the Velocity Command Manager for this plugin
     */
    public CommandManager<CommandSource> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns the config manager for this plugin.
     * @return the config manager for this plugin
     */
    public VelocityConfigManager configManager() {
        return configManager;
    }

    /**
     * Returns the messages config for this plugin.
     * @return the messages config for this plugin
     */
    public MessagesConfig messagesConfig() {
        return configManager.getConfig("messages", MessagesConfig.class);
    }

    /**
     * Returns the utility section of the messages config for this plugin.
     * @return the utility section of the messages config for this plugin
     */
    public MessagesConfig.Utility utility() {
        return configManager.getConfig("messages", MessagesConfig.class).utility();
    }

    /**
     * Returns the commands section of the messages config for this plugin.
     * @return the commands section of the messages config for this plugin
     */
    public MessagesConfig.Commands commands() {
        return configManager.getConfig("messages", MessagesConfig.class).commands();
    }

    /**
     * Returns the translation manager for this plugin.
     * @return the translation manager for this plugin
     */
    public TranslationManager translationManager() {
        return translationManager;
    }

    /**
     * Returns the proxy server.
     * @return the proxy server
     */
    public ProxyServer getServer() {
        return server;
    }

    /**
     * Returns the Velocity Connector Plugin instance.
     * See https://github.com/Phoenix616/ConnectorPlugin
     * @return the Velocity Connector Plugin instance
     */
    public VelocityConnectorPlugin connector() {
        return this.connector;
    }
    

    /**
     * Registers all commands for this plugin.
     * Each class contains cloud commands for a specific category.
     */
    private void registerCommands() {
        Stream.of(
            new BackCommand(this),
            new ListCommand(this),
            new LocateCommand(this),
            new TeleportAcceptCommand(this),
            new TeleportCancelCommand(this),
            new TeleportDenyCommand(this),
            new TeleportCommand(this),
            new TeleportHereCommand(this),
            new TeleportHereRequestCommand(this),
            new TeleportPositionCommand(this),
            new TeleportRequestCommand(this),
            new VParadisuCommand(this),
            new WarpCommand(this)
        ).forEach(AbstractVelocityCommand::register);
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded plugin");
    }

}