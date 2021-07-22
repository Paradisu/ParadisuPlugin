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
        return false;
    }
}
