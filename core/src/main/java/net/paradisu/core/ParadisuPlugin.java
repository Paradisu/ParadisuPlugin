package net.paradisu.core;

import cloud.commandframework.CommandManager;
import de.themoep.connectorplugin.ConnectorPlugin;
import net.paradisu.core.config.ConfigManager;
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
    public void reload();
}
