package net.paradisu.core;

import cloud.commandframework.CommandManager;
import de.themoep.connectorplugin.ConnectorPlugin;
import org.slf4j.Logger;

import java.nio.file.Path;


public interface ParadisuPlugin {
    public Logger logger();
    public Path dataDirectory();
    public CommandManager<?> commandManager();
    public ConnectorPlugin<?> connector();
    public void reload();
}
