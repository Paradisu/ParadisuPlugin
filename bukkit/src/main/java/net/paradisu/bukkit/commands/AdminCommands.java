package net.paradisu.bukkit.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import net.luckperms.api.LuckPermsProvider;
import net.paradisu.bukkit.ParadisuMain;

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

    @CommandPermission("paradisu.mkill")
    @CommandMethod("minecartkill|mkill <radius>")
    public void mKill(CommandSender sender,
            @Argument("radius") int radius) {

        Player player = (Player) sender;
        List<Entity> allEntites = player.getNearbyEntities(radius, radius, radius);
        int entityCount = 0;

        if (radius < 1 || radius > 10) {
            player.sendMessage(cmdprefix + "§fThe radius has to be between 1 and 10.");
            return;
        }

        for (Entity entity : allEntites) {
            if (entity instanceof RideableMinecart) {
                entity.remove();
                entityCount++;
            }
        }

        if (entityCount == 0) { 
            player.sendMessage(cmdprefix + "§fNo minecarts found within " + radius + " blocks.");
        } else {
            player.sendMessage(cmdprefix + "§fKilled " + entityCount + " minecarts.");
            player.playSound(player, Sound.BLOCK_ANCIENT_DEBRIS_BREAK, SoundCategory.MASTER, 1F, 1F);
        }
    
    }

}
