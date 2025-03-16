package net.paradisu.velocity;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;
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
import net.paradisu.core.packs.PackManager;
import net.paradisu.core.utils.Constants;
import net.paradisu.velocity.commands.VelocityCommandRegistrar;
import net.paradisu.velocity.config.VelocityConfigManager;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.velocity.config.configs.ParadisuConfig;
import net.paradisu.velocity.pack.PackListener;
import net.paradisu.velocity.util.VelocityLogger;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = Constants.Plugin.ID, name = Constants.Plugin.NAME, version = Constants.Plugin.VERSION, description = Constants.Plugin.DESCRIPTION, authors = {
        Constants.Plugin.AUTHORS }, url = Constants.Plugin.URL, dependencies = @Dependency(id = "connectorplugin"))
public final class ParadisuVelocity implements ParadisuPlugin {
    private final ProxyServer server;
    private final VelocityLogger logger;
    private final Path dataDirectory;
    private VelocityConfigManager configManager;
    private TranslationManager translationManager;
    private VelocityCommandManager<CommandSource> commandManager;
    private boolean connectorEnabled;
    private VelocityConnectorPlugin connector;
    private PackManager packManager;


    @Inject
    public ParadisuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = new VelocityLogger(logger);
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize the config manager
        this.configManager = new VelocityConfigManager(this);
        this.configManager.loadConfigs();
        // Initialize the pack manager
        this.packManager = new PackManager(this, this.paradisuConfig().resourcePackUrls());
        // Initialize the translation manager
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();

        // Initialize the command manager
        this.commandManager = new VelocityCommandManager<>(
                this.server.getPluginManager().ensurePluginContainer(this),
                this.server,
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity());
        VelocityCommandRegistrar.registerCommands(this);

        // Initialize the connector plugin
        Optional<PluginContainer> connectorPlugin = server().getPluginManager().getPlugin("connectorplugin");
        connectorEnabled = connectorPlugin.isPresent();
        // Debug logging: was the connector plugin found?
        logger().info("ConnectorPlugin found: " + connectorEnabled);
        if (connectorEnabled) {
            this.connector = (VelocityConnectorPlugin) connectorPlugin.get().getInstance().get();
            // Debug logging: what is the connector plugin bridge?
            logger().info("ConnectorPlugin instance: " + this.connector.getBridge().toString());
            logger().info("ConnectorPlugin server: " + this.connector.getServerName());
        }

        // Register the pack listener
        this.server().getEventManager().register(this, new PackListener(this, this.packManager()));
    }

    /**
     * Returns the logger for this plugin.
     * 
     * @return the logger for this plugin
     */
    public VelocityLogger logger() {
        return this.logger;
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
        return this.configManager;
    }

    /**
     * Returns the messages config for this plugin.
     * 
     * @return the messages config for this plugin
     */
    public MessagesConfig messagesConfig() {
        return this.configManager().getConfig("messages", MessagesConfig.class);
    }

    public ParadisuConfig paradisuConfig() {
        return this.configManager().getConfig("paradisu", ParadisuConfig.class);
    }

    /**
     * Returns the translation manager for this plugin.
     * 
     * @return the translation manager for this plugin
     */
    public TranslationManager translationManager() {
        return this.translationManager;
    }

    /**
     * Returns the proxy server.
     * 
     * @return the proxy server
     */
    public ProxyServer server() {
        return this.server;
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
     * Returns the Pack Manager for this plugin.
     * 
     * @return the Pack Manager for this plugin
     */
    public PackManager packManager() {
        return this.packManager;
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