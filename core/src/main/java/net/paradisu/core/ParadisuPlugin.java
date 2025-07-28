package net.paradisu.core;

import org.incendo.cloud.CommandManager;
import de.themoep.connectorplugin.ConnectorPlugin;
import net.paradisu.core.config.ConfigManager;
import net.paradisu.core.database.DatabaseSession;
import net.paradisu.core.locale.TranslationManager;
import net.paradisu.core.utils.ParadisuLogger;

import java.nio.file.Path;


public interface ParadisuPlugin {
    public ParadisuLogger logger();
    public Path dataDirectory();
    public CommandManager<?> commandManager();
    public ConfigManager configManager();
    public TranslationManager translationManager();
    public ConnectorPlugin<?> connector();
    public DatabaseSession databaseSession();
    public void reload();
}
