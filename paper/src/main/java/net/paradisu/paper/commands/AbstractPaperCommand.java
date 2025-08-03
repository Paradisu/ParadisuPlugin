package net.paradisu.paper.commands;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.paradisu.core.commands.AbstractCommand;
import net.paradisu.core.commands.HelpManager;
import net.paradisu.paper.ParadisuPaper;

public abstract class AbstractPaperCommand implements AbstractCommand {
    protected final ParadisuPaper paradisu;
    protected final CommandManager<CommandSourceStack> commandManager;
    protected final HelpManager<CommandSourceStack> helpManager;

    public AbstractPaperCommand(ParadisuPaper paradisu) {
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
        this.helpManager = new HelpManager<CommandSourceStack>(MinecraftHelp.create("/paradisu help", this.commandManager, p -> p.getSender()));
    }

    @Override
    public abstract void register();
}
