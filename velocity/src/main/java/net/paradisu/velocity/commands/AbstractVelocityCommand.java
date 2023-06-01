package net.paradisu.velocity.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.velocitypowered.api.command.CommandSource;
import net.paradisu.core.commands.AbstractCommand;
import net.paradisu.core.commands.HelpManager;
import net.paradisu.velocity.ParadisuVelocity;

public abstract class AbstractVelocityCommand implements AbstractCommand {
    protected final ParadisuVelocity paradisu;
    protected final CommandManager<CommandSource> commandManager;
    protected final HelpManager<CommandSource> helpManager;

    /**
     * Constructor for the AbstractCommand class.
     * @param paradisu The instance of the Paradisu plugin.
     */
    public AbstractVelocityCommand(ParadisuVelocity paradisu) {
        // Initialize the command manager
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
        this.helpManager = new HelpManager<>(new MinecraftHelp<>("/vparadisu help", p -> p, this.commandManager));
    }

    @Override
    public abstract void register();
}