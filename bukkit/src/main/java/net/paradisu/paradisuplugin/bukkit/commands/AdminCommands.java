package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;

public class AdminCommands {
    
    @CommandPermission("paradisu.world")
    @CommandMethod("world")
    public void world(CommandSender sender){
        Player p = (Player) sender;
        p.sendMessage("curr world: " + p.getWorld().getName());
    }

}
