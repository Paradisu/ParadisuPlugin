/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.paper;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.database.DatabaseSession;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.paper.commands.PaperCommandRegistrar;
import net.paradisu.paper.config.PaperConfigManager;
import net.paradisu.paper.config.configs.MessagesConfig;
import net.paradisu.paper.config.configs.ParadisuConfig;
import net.paradisu.paper.listeners.PlayerJoinListener;
import net.paradisu.paper.listeners.PlayerQuitListener;
import net.paradisu.paper.util.PaperLogger;
import net.paradisu.paper.util.SafeItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

import java.nio.file.Path;

public class ParadisuPaper extends JavaPlugin implements ParadisuPlugin {
    private PaperCommandManager<CommandSourceStack> commandManager;
    private PaperLogger logger;
    private BukkitConnectorPlugin connector;
    private boolean connectorEnabled;
    private PaperConfigManager configManager;
    private TranslationManager translationManager;
    private DatabaseSession databaseSession;
    private SafeItemSerializer safeItemSerializer;

    @Override
    public void onLoad() {
        this.logger = new PaperLogger(getLogger());
        this.safeItemSerializer = new SafeItemSerializer(this.logger);
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
                this.connector =
                        (BukkitConnectorPlugin) Bukkit.getPluginManager().getPlugin("ConnectorPlugin");
            }

            this.databaseSession = new DatabaseSession(
                    this,
                    this.paradisuConfig().database().url(),
                    this.paradisuConfig().database().username(),
                    this.paradisuConfig().database().password());

            this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
            this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        } catch (Exception e) {
            this.logger.error("Failed to enable ParadisuPlugin", e);
        }
    }

    /**
     * Returns the PaperCommandManager for this plugin.
     *
     * @return the PaperCommandManager for this plugin
     */
    @Override
    public CommandManager<CommandSourceStack> commandManager() {
        return commandManager;
    }

    /**
     * Returns the Bukkit Connector Plugin instance.
     *
     * @return the Bukkit Connector Plugin instance
     */
    @Override
    public BukkitConnectorPlugin connector() {
        return connector;
    }

    /**
     * Returns whether the Connector Plugin is enabled.
     *
     * @return true if the Connector Plugin is enabled, false otherwise
     */
    @Override
    public Path dataDirectory() {
        return getDataFolder().toPath();
    }

    /**
     * Returns the translation manager for this plugin.
     *
     * @return the translation manager for this plugin
     */
    @Override
    public TranslationManager translationManager() {
        return translationManager;
    }

    /**
     * Returns the config manager for this plugin.
     *
     * @return the config manager for this plugin
     */
    @Override
    public PaperConfigManager configManager() {
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
     * Returns the ParadisuConfig for this plugin.
     *
     * @return the ParadisuConfig for this plugin
     */
    public ParadisuConfig paradisuConfig() {
        return configManager.getConfig("paradisu", ParadisuConfig.class);
    }

    /**
     * Returns the logger for this plugin.
     *
     * @return the logger for this plugin
     */
    @Override
    public PaperLogger logger() {
        return logger;
    }

    /**
     * Returns the database session for this plugin.
     *
     * @return the database session for this plugin
     */
    @Override
    public DatabaseSession databaseSession() {
        return databaseSession;
    }

    /**
     * Returns the SafeItemSerializer for this plugin.
     *
     * @return the SafeItemSerializer for this plugin
     */
    public SafeItemSerializer safeItemSerializer() {
        return safeItemSerializer;
    }

    /** Reloads the plugin. */
    @Override
    public void reload() {
        this.configManager.loadConfigs();
        this.translationManager.reload();
        logger().info("Reloaded Paradisu Paper plugin");
    }
}
