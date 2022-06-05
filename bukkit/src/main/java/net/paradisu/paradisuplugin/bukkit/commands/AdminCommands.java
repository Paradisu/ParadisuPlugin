package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.luckperms.api.LuckPermsProvider;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class AdminCommands {
    
    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    @CommandPermission("paradisu.world")
    @CommandMethod("world")
    public void world(CommandSender sender){
        Player player = (Player) sender;
        player.sendMessage(cmdprefix + "§fCurrent World: " + cmdemph + player.getWorld().getName());
        player.sendMessage(cmdprefix + "§fLuckperms server ID: " + cmdemph + LuckPermsProvider.get().getServerName().toString());
    }

}
