package me.jakedadream.snwplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;

public class warps implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player = (Player) sender;


        /*
        ============================================
        Warping
        ============================================
         */
        if (cmd.getName().equalsIgnoreCase("warp")) {
            if (args.length == 0) {
                player.sendMessage("§3[§dParadisu §bツ§3] §f/warp §3(name)");
                return true;
            }
            if (getConfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase()) == null) {
                player.sendMessage("§3[§dParadisu §bツ§3] §c" + args[0] + " §fis not a valid warp");
                return true;
            }

            ConfigurationSection wi = getConfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase());
            Location loc = new Location(Bukkit.getWorld(wi.getString("world")), wi.getDouble("X"), wi.getDouble("Y"), wi.getDouble("Z"), wi.getInt("yaw"), wi.getInt("pitch"));
            player.sendMessage("§3[§dParadisu §bツ§3] §fTeleporting to§3 " + args[0]);
            player.teleport(loc);
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            return true;
        }

        /*
        ============================================
        Warp list
        ============================================
         */
        if (cmd.getName().equalsIgnoreCase("warps")) {
            Set<String> list = getConfig().getConfigurationSection("warps").getKeys(false);
            player.sendMessage(getConfig().getString("§3[§dParadisu §bツ§3] §fHere is a list of warps: §c" + list.toString());
            return true;
        }

        /*
        ============================================
        Setting and deleting warps
        ============================================
         */

        if (cmd.getName().equalsIgnoreCase("setwarp")) {
            if (args.length == 0) {
                player.sendMessage(getConfig().getString("message-prefix") + "§r /setwarp (name)");
                return true;
            }
            Location loc = player.getLocation();
            getConfig().getConfigurationSection("warps").createSection(args[0].toLowerCase());
            ConfigurationSection wi = getConfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase());
            wi.set("X", Double.valueOf(loc.getX()));
            wi.set("Y", Double.valueOf(loc.getY()));
            wi.set("Z", Double.valueOf(loc.getZ()));
            wi.set("world", loc.getWorld().getName());
            wi.set("yaw", Float.valueOf(loc.getYaw()));
            wi.set("pitch", Float.valueOf(loc.getPitch()));
            saveConfig();
            player.sendMessage("§3[§dParadisu §bツ§3] §rCreated new warp named §3" + args[0] + " §fat location §3" + loc.getBlockX() + ",§3" + loc.getBlockY() + ",§3" + loc.getBlockZ() + " §fin world§3 " + loc.getWorld().getName());
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delwarp")) {
            if (args.length == 0) {
                player.sendMessage("§3[§dParadisu §bツ§3] §r/delwarp (name)");
                return true;
            }
            if (getConfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase()) == null) {
                player.sendMessage("§3[§dParadisu §bツ§3] §rThe specified warp does not exist");
                return true;
            }
            getConfig().getConfigurationSection("warps").set(args[0], null);
            saveConfig();
            reloadConfig();
            player.sendMessage("§3[§dParadisu §bツ§3] §rSuccessfully deleted §c" + args[0]);
            return true;
        }

        return false;
    }
    /* Made by Llamaz */
}
