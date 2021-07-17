package me.jakedadream.ParadisuPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class warps implements CommandExecutor {

    private Plugin plugin;
    private FileConfiguration conf;
    private File file;

    private final String fileName = "warps.yml";

    private String getParsedName(String[] args) {
        String name = "";

        // Concat all the args to a string
        for (int i = 0; i < args.length; i++) {
            name = name.concat(args[i]);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }



    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
/*
        if (cmd.getName().equalsIgnoreCase("createwarp")) {

            Location loc = player.getLocation();
            ConfigurationSection wi = snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("Warp List").getConfigurationSection(getParsedName(args).toLowerCase());
            wi.set("X", loc.getX());
            wi.set("Y", loc.getY());
            wi.set("Z", loc.getZ());
            wi.set("Pitch", loc.getPitch());
            wi.set("Yaw", loc.getYaw());

            file = new File("warps.yml");
            conf = YamlConfiguration.loadConfiguration(file);

            snwplugin.getPlugin(snwplugin.class).getWarpsconfig().save(file);



        }

        if (cmd.getName().equalsIgnoreCase("warp")) {

        }

        if (cmd.getName().equalsIgnoreCase("warps")) {

        }

        if (cmd.getName().equalsIgnoreCase("delwarp")) {

        }


        /*






                            //snwplugin.getPlugin(snwplugin.class)
        ============================================
        Warping
        ============================================









        if (cmd.getName().equalsIgnoreCase("warp")) {
            if (args.length == 0) {
                player.sendMessage("§3[§dParadisu §bツ§3] §f/warp §3(name)");
                return true;
            }
            if (snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase()) == null) {
                player.sendMessage("§3[§dParadisu §bツ§3] §c" + args[0] + " §fis not a valid warp");
                return true;
            }

            ConfigurationSection wi = snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase());
            Location loc = new Location(Bukkit.getWorld(wi.getString("world")), wi.getDouble("X"), wi.getDouble("Y"), wi.getDouble("Z"), wi.getInt("yaw"), wi.getInt("pitch"));
            player.sendMessage("§3[§dParadisu §bツ§3] §fTeleporting to§3 " + args[0]);
            player.teleport(loc);
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            return true;
        }

        ============================================
        Warp list
        ============================================

        if (cmd.getName().equalsIgnoreCase("warps")) {
            Set<String> list = snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").getKeys(false);
            player.sendMessage(snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getString("§3[§dParadisu §bツ§3] §fHere is a list of warps: §c" + list.toString()));
            return true;
        }


        ============================================
        Setting and deleting warps

        if (cmd.getName().equalsIgnoreCase("setwarp")) {
            if (args.length == 0) {
                player.sendMessage("§3[§dParadisu §bツ§3]§f /setwarp (name)");
                return true;
            }
            Location loc = player.getLocation();
            snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").createSection(args[0].toLowerCase());
            ConfigurationSection wi = snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase());







            snwplugin.getPlugin(snwplugin.class).getWarpsconfig().save(conf);
            player.sendMessage("§3[§dParadisu §bツ§3] §rCreated new warp named §3" + args[0] + " §fat location §3" + loc.getBlockX() + ",§3" + loc.getBlockY() + ",§3" + loc.getBlockZ() + " §fin world§3 " + loc.getWorld().getName());
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delwarp")) {
            if (args.length == 0) {
                player.sendMessage("§3[§dParadisu §bツ§3] §r/delwarp (name)");
                return true;
            }
            if (snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").getConfigurationSection(args[0].toLowerCase()) == null) {
                player.sendMessage("§3[§dParadisu §bツ§3] §rThe specified warp does not exist");
                return true;
            }
            snwplugin.getPlugin(snwplugin.class).getWarpsconfig().getConfigurationSection("warps").set(args[0], null);
            saveConfig();
            reloadConfig();
            player.sendMessage("§3[§dParadisu §bツ§3] §rSuccessfully deleted §c" + args[0]);
            return true;
        }

        return false;
    }
*/
        /* Made by Llamaz */
        return false;
    }
}
