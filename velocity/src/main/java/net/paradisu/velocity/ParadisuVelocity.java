package net.paradisu.velocity;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.themoep.connectorplugin.velocity.VelocityConnectorPlugin;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.core.utils.Constants;
import net.paradisu.velocity.commands.AbstractVelocityCommand;
import net.paradisu.velocity.config.VelocityConfigManager;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.velocity.util.VelocityLogger;
import org.reflections.Reflections;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Plugin(id = Constants.Plugin.ID, name = Constants.Plugin.NAME, version = Constants.Plugin.VERSION, description = Constants.Plugin.DESCRIPTION, authors = {
        Constants.Plugin.AUTHORS }, url = Constants.Plugin.URL, dependencies = @Dependency(id = "connectorplugin"))
public final class ParadisuVelocity implements ParadisuPlugin {
    private final ProxyServer server;
    private final VelocityLogger velocityLogger;
    private final Path dataDirectory;
    private VelocityConfigManager configManager;
    private TranslationManager translationManager;
    private VelocityCommandManager<CommandSource> commandManager;
    private boolean connectorEnabled;
    private VelocityConnectorPlugin connector;

    @Inject
    public ParadisuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.velocityLogger = new VelocityLogger(logger);
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
                Function.identity());
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
     * 
     * @return the logger for this plugin
     */
    public VelocityLogger logger() {
        return this.velocityLogger;
    }

    /**
     * Returns the data directory for this plugin.
     * 
     * @return the data directory for this plugin
     */
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    /**
     * Returns the Velocity Command Manager for this plugin.
     * 
     * @return the Velocity Command Manager for this plugin
     */
    public CommandManager<CommandSource> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns the config manager for this plugin.
     * 
     * @return the config manager for this plugin
     */
    public VelocityConfigManager configManager() {
        return configManager;
    }

    /**
     * Returns the messages config for this plugin.
     * 
     * @return the messages config for this plugin
     */
    public MessagesConfig messagesConfig() {
        return configManager.getConfig("messages", MessagesConfig.class);
    }

    /**
     * Returns the translation manager for this plugin.
     * 
     * @return the translation manager for this plugin
     */
    public TranslationManager translationManager() {
        return translationManager;
    }

    /**
     * Returns the proxy server.
     * 
     * @return the proxy server
     */
    public ProxyServer getServer() {
        return server;
    }

    /**
     * Returns the Velocity Connector Plugin instance.
     * See https://github.com/Phoenix616/ConnectorPlugin
     * 
     * @return the Velocity Connector Plugin instance
     */
    public VelocityConnectorPlugin connector() {
        return this.connector;
    }

    /**
     * Registers all commands for this plugin via reflection.
     * Each class contains cloud commands for a specific category.
     */
    private void registerCommands() {
        Reflections reflections = new Reflections("net.paradisu.velocity.commands.command");
        Set<Class<? extends AbstractVelocityCommand>> classes = reflections
                .getSubTypesOf(AbstractVelocityCommand.class);
        for (Class<? extends AbstractVelocityCommand> clazz : classes) {
            try {
                Constructor<? extends AbstractVelocityCommand> constructor = clazz.getDeclaredConstructor(this.getClass());
                AbstractVelocityCommand command = constructor.newInstance(this);
                command.register();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded Paradisu Velocity plugin");
    }

}