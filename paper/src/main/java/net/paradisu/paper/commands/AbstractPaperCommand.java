package net.paradisu.paper.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.paradisu.core.commands.AbstractCommand;
import net.paradisu.core.commands.HelpManager;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.command.CommandSender;

public abstract class AbstractPaperCommand implements AbstractCommand {
    protected final ParadisuPaper paradisu;
    protected final CommandManager<CommandSender> commandManager;
    protected final HelpManager<CommandSender> helpManager;
    protected final BukkitAudiences audiences;

    public AbstractPaperCommand(ParadisuPaper paradisu) {
        this.audiences = BukkitAudiences.create(paradisu);
        this.paradisu = paradisu;
        this.commandManager = paradisu.commandManager();
        this.helpManager = new HelpManager<>(new MinecraftHelp<>("/paradisu help", audiences::sender, this.commandManager));
    }

    @Override
    public abstract void register();
}
