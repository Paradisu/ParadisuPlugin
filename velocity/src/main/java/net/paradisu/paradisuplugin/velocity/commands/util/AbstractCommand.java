package net.paradisu.paradisuplugin.velocity.commands.util;

import com.velocitypowered.api.command.CommandSource;

import cloud.commandframework.CommandManager;
import net.paradisu.paradisuplugin.velocity.Paradisu;

public abstract class AbstractCommand {
    protected final  Paradisu paradisu;
    protected final CommandManager<CommandSource> commandManager;

    public AbstractCommand(Paradisu paradisu) {
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
    }

    public abstract void register();
}