package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
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

    @CommandPermission("paradisu.invsee.admin")
    @ProxiedBy("admininvsee")
    @CommandMethod("invsee admin <player>")
    public void adminInvsee(CommandSender sender,
            @Argument("player") String player) {
        Player p = (Player) sender;
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            p.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
            return;
        }
        p.openInventory(target.getInventory());
        p.sendMessage(cmdprefix + "§fOpening the inventory of §3" + target.getName() + ".");
    }


}
