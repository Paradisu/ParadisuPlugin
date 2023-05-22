package net.paradisu.core;

import cloud.commandframework.CommandManager;
import de.themoep.connectorplugin.ConnectorPlugin;

import java.nio.file.Path;
import java.util.logging.Logger;

public interface ParadisuPlugin {
    public Logger logger();
    public Path dataDirectory();
    public CommandManager<?> commandManager();
    public ConnectorPlugin<?> connector();
    public void registerCommands();
    public void reload();
}
