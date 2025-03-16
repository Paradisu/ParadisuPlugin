package net.paradisu.velocity.commands;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
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
        this.helpManager = new HelpManager<>(MinecraftHelp.create("/vparadisu help", this.commandManager, p -> p));
    }

    @Override
    public abstract void register();
}