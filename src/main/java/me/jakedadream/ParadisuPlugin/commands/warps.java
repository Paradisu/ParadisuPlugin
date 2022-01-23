package me.jakedadream.ParadisuPlugin.commands;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
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
import java.util.Set;

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

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();
    String nopermsmsg = paradisumain.NoPermsMessage();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()){

            case "setwarp":
                if (!(player.hasPermission("snw.warp.set"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /setwarp <warp name>");
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
                player.sendMessage(cmdprefix + "§fCreated warp " + cmdemph + args[0] + "&f at your location");
                break;

            case "delwarp":
                if(!(player.hasPermission("snw.warp.del"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /delwarp <warp name>");
                    return true;
                }
                paradisumain.fileWarpConfig.set(args[0], null);
                paradisumain.saveWarpConfig();

                String delname = args[0];
                delname = delname.substring(0,1).toUpperCase() + delname.substring(1);


                player.sendMessage(cmdprefix + "§fDeleted warp " + cmdemph + delname + "§f.");
                break;

            case "warpdisplay":
            case "wdisplay":
                if (!(player.hasPermission("snw.warp.display"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                String displayname = "";
//                String sourcewarp = args[0];
                for (int i = 0; i < args.length; i++){
                    if (i == 0) continue;
                    if (i == args.length - 1){
                        displayname += args[i];
                        continue;
                    }
                    displayname += args[i] + " ";
                }
                paradisumain.fileWarpConfig.getConfigurationSection(args[0].toLowerCase()).set("display", displayname);
                paradisumain.saveWarpConfig();
                player.sendMessage(cmdprefix + "§fCreated warp display name " + cmdemph + displayname);
                break;

            case "w":
            case "warp":
                if(args.length == 0){
                    player.sendMessage(cmdprefix + "§fUsage: /warp <warp name>");
                    return true;
                }
                ConfigurationSection d = paradisumain.fileWarpConfig.getConfigurationSection(args[0].toLowerCase());
                if (d == null){
                    if (paradisumain.fileWarpConfig.getString("aliases." + args[0].toLowerCase()) != null) {
                        String w = paradisumain.fileWarpConfig.getString("aliases." + args[0].toLowerCase());
                        d = paradisumain.fileWarpConfig.getConfigurationSection(w);
                    } else {
                        player.sendMessage(cmdprefix + "§fThat warp doesn't exist.");
                        return true;
                    }
                }
                if (!(player.hasPermission(d.getString("Permission")))){
                    player.sendMessage(cmdprefix + "§fYou cannot warp here.");
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

                //

                String wstring;
                if (d.getString("display") != null){
                    wstring = d.getString("display");
                } else {
                    wstring = d.getName();
                    wstring = wstring.substring(0,1).toUpperCase() + wstring.substring(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                player.teleport(l);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 29);
                player.sendMessage(cmdprefix + "§fWelcome to " + cmdemph +  wstring + "§f!");
                break;

            case "reloadwarp":
            case "reloadwarps":
                if(!(player.hasPermission("snw.warp.reload"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                paradisumain.reloadWarpConfig();
                player.sendMessage(cmdprefix + "§fReloaded warps.");
                break;

            case "setalias":
                if(!(player.hasPermission("snw.warp.setalias"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if(args.length < 2 || paradisumain.fileWarpConfig.getConfigurationSection(args[0].toLowerCase()) == null){
                    player.sendMessage(cmdprefix + "§fUsage: /setalias <warp> <alias>");
                    return true;
                }
                ConfigurationSection al = paradisumain.fileWarpConfig.getConfigurationSection("aliases");
                al.set(args[1].toLowerCase(), args[0].toLowerCase());
                paradisumain.saveWarpConfig();
                player.sendMessage(cmdprefix + cmdemph + args[1] + "§f is now a warp alias for " + cmdemph + args[0] + "§f!");
                break;

            case "delalias":
                if(!(player.hasPermission("snw.warp.delalias"))){
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if(args.length < 2 || paradisumain.fileWarpConfig.getConfigurationSection(args[0].toLowerCase()) == null || paradisumain.fileWarpConfig.getString("aliases." + args[1].toLowerCase()) == null){
                    player.sendMessage(cmdprefix + "§fUsage: use /delalias <warp> <alias>");
                    return true;
                }
                paradisumain.fileWarpConfig.getConfigurationSection("aliases").set(args[1].toLowerCase(), null);
                paradisumain.saveWarpConfig();
                player.sendMessage(cmdprefix + "§fDeleted warp alias " + cmdemph + args[1]);
                break;

            case "warps":
                Set<String> s = paradisumain.fileWarpConfig.getKeys(false);
                s.remove("aliases");
                String mes = cmdprefix + "§fCurrent Warps:" + cmdemph;
                FileConfiguration warplist = paradisumain.fileWarpConfig;
                for (String i : s){
//                      player.sendMessage(mes);
                    if(warplist.getConfigurationSection(i).getString("display") != null){
                        mes = mes.concat("\n" + "§f- "  + cmdemph + warplist.getConfigurationSection(i).getString("display"));
                    } else {
                        i = i.substring(0,1).toUpperCase() + i.substring(1);
                        mes = mes.concat("\n" + "§f- " + cmdemph + i);
                    }
                }
                player.sendMessage(mes);
                break;

//                 String wstring = d.getName();
//                wstring = wstring.substring(0,1).toUpperCase() + wstring.substring(1);

        }
        return false;
    }
}
