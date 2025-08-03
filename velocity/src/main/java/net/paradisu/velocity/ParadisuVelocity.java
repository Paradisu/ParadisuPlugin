package net.paradisu.velocity;

import org.hibernate.Session;
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
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.database.DatabaseSession;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.core.packs.PackManager;
import net.paradisu.core.utils.Constants;
import net.paradisu.velocity.commands.VelocityCommandRegistrar;
import net.paradisu.velocity.config.VelocityConfigManager;
import net.paradisu.velocity.config.configs.MessagesConfig;
import net.paradisu.velocity.config.configs.ParadisuConfig;
import net.paradisu.velocity.listeners.ConnectionListener;
import net.paradisu.velocity.listeners.LimboListener;
import net.paradisu.velocity.listeners.PackListener;
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
    private VelocityConnectorPlugin connector;
    private PackManager packManager;
    private DatabaseSession databaseSession;


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
        // Initialize the database session
        this.databaseSession = new DatabaseSession(
                this,
                this.paradisuConfig().database().url(),
                this.paradisuConfig().database().username(),
                this.paradisuConfig().database().password());
        if (this.databaseSession.open()) {
            // Run Liquibase migrations
            // This will occur before players can connect to the server
            try (var em = this.databaseSession.factory().createEntityManager()) { // Create an EntityManager
                em.getTransaction().begin(); // Start a transaction to access the session

                // Unwrap the EntityManager to get the Hibernate Session
                em.unwrap(Session.class).doWork(connection -> {
                    ClassLoader previousContextClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                    try (
                        Liquibase liquibase = new Liquibase(
                            "db/migrations.xml",
                            new ClassLoaderResourceAccessor(),
                            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
                        )
                    ) {
                        liquibase.update(new Contexts(), new LabelExpression());
                        this.logger.info("Database migrations applied successfully.");
                    } catch (LiquibaseException e) {
                        this.logger.error("Failed to migrate database", e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(previousContextClassLoader);
                    }
                });
            } catch (Exception e) {
                this.logger.error("An error occurred during database migration.", e);
            }
        } else {
            this.logger.error("Failed to open database session. The plugin will not function correctly without a valid database connection.");
        }
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
        if (connectorPlugin.isPresent()) {
            this.connector = (VelocityConnectorPlugin) connectorPlugin.get().getInstance().get();
        }

        // Register listeners
        this.server().getEventManager().register(this, new PackListener(this, this.packManager()));
        this.server().getEventManager().register(this, new LimboListener(this));
        this.server().getEventManager().register(this, new ConnectionListener(this));
    }

    /**
     * Returns the logger for this plugin.
     * 
     * @return the logger for this plugin
     */
    @Override
    public VelocityLogger logger() {
        return this.logger;
    }

    /**
     * Returns the data directory for this plugin.
     * 
     * @return the data directory for this plugin
     */
    @Override
    public Path dataDirectory() {
        return this.dataDirectory;
    }

    /**
     * Returns the Velocity Command Manager for this plugin.
     * 
     * @return the Velocity Command Manager for this plugin
     */
    @Override
    public CommandManager<CommandSource> commandManager() {
        return this.commandManager;
    }

    /**
     * Returns the config manager for this plugin.
     * 
     * @return the config manager for this plugin
     */
    @Override
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
    @Override
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
    @Override
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
     * Returns the database session for this plugin.
     * 
     * @return the database session for this plugin
     */
    @Override
    public DatabaseSession databaseSession() {
        return this.databaseSession;
    }

    /**
     * Reloads the plugin.
     */
    @Override
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded Paradisu Velocity plugin");
    }
}