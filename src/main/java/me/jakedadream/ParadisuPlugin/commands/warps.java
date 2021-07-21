package me.jakedadream.ParadisuPlugin.commands;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()){

            case "setwarp":
                if (!(player.hasPermission("snw.warp.set"))){
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage("§3[§dParadisu §bツ§3] §fUsage: /setwarp <warp name>");
                    return true;
                }

                Location loc = player.getLocation();
                paradisumain.fileWarpConfig.createSection(args[0].toLowerCase());
                ConfigurationSection cs = paradisumain.fileWarpConfig.getConfigurationSection(args[0].toLowerCase());

                cs.set("X", loc.getX());
                cs.set("Y", loc.getY());
                cs.set("Z", loc.getZ());
                cs.set("Yaw", loc.getYaw());
                cs.set("Pitch", loc.getPitch());
                cs.set("World", loc.getWorld().getName());
                cs.set("Permission", "snw.warp.default");

                paradisumain.saveWarpConfig();
                player.sendMessage("§3[§dParadisu §bツ§3] §fCreated warp " + ChatColor.DARK_AQUA + args[0] + ChatColor.WHITE + " at your location");
                break;

            case "delwarp":
                if(!(player.hasPermission("snw.warp.del"))){
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage("§3[§dParadisu §bツ§3] §f Usage: /delwarp <warp name>");
                    return true;
                }
                paradisumain.fileWarpConfig.set(args[0], null);
                paradisumain.saveWarpConfig();

                String delname = args[0];
                delname = delname.substring(0,1).toUpperCase() + delname.substring(1);


                player.sendMessage("§3[§dParadisu §bツ§3] §fDeleted warp " + ChatColor.DARK_AQUA + delname + ChatColor.WHITE + ".");
                break;

            case "w":
            case "warp":
                if(args.length == 0){
                    player.sendMessage("§3[§dParadisu §bツ§3] §fUsage: /warp <place>");
                    return true;
                }
                ConfigurationSection d = paradisumain.fileWarpConfig.getConfigurationSection(args[0]);
                if (d == null){
                    player.sendMessage("§3[§dParadisu §bツ§3] §fThat warp doesn't exist.");
                    return true;
                } else {
//                    player.sendMessage(String.valueOf(d.getDouble("X")));

                }
                if (!(player.hasPermission(d.getString("Permission")))){
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You cannot warp here.");
                    return true;
                }

                //def shouldn't have to set this to player loc but idk it didn't like it when i left it null
                Location l = player.getLocation();


//                player.sendMessage(String.valueOf(d.getDouble("X")));

                l.setX(d.getDouble("X"));
                l.setY(d.getDouble("Y"));
                l.setZ(d.getDouble("Z"));
                l.setPitch((float) d.getDouble("Pitch"));
                l.setYaw((float) d.getDouble("Yaw"));
                l.setWorld(Bukkit.getServer().getWorld(d.getString("World")));

                String wstring = d.getName();
                wstring = wstring.substring(0,1).toUpperCase() + wstring.substring(1);

                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                player.teleport(l);
                player.sendMessage("§3[§dParadisu §bツ§3] §fWelcome to " + ChatColor.DARK_AQUA +  wstring + ChatColor.WHITE + "!");

        }




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
