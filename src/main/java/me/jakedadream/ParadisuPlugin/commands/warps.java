package me.jakedadream.ParadisuPlugin.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.sql.DataSource;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jakedadream.ParadisuPlugin.ParadisuMain;
import me.jakedadream.ParadisuPlugin.databaseHandlers.WarpsDataHandler;
import me.jakedadream.ParadisuPlugin.wrappers.Warp;

public class warps implements CommandExecutor {

    private static DataSource dataSource;

    public warps(){
        dataSource = ParadisuMain.getDBCon();

    }

    
    // private String getParsedName(String[] args) {
    //     String name = "";

    //     // Concat all the args to a string
    //     for (int i = 0; i < args.length; i++) {
    //         name = name.concat(args[i]);
    //         name = name.concat(" ");
    //     }
    //     return ChatColor.translateAlternateColorCodes('&', name);
    // }

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;
        

        switch (cmd.getName().toLowerCase()) {

            case "setwarp":

                if (!(player.hasPermission("snw.warp.set"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");
                    return true;
                }

                try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO Warps (WarpName, X_pos, Y_pos, Z_pos, Pitch, Yaw, World, Permission, DisplayName) VALUES (?,?,?,?,?,?,?,?,?)")){
                    
                    
                    if (args.length == 1) {
                        ps.setNull(9, java.sql.Types.VARCHAR);
                    } else {
                        String displayName = "";
                        for (int i = 1; i < args.length; i++) {
                            displayName = displayName.concat(args[i]);
                            displayName = displayName.concat(" ");
                        }
                        ps.setString(9, displayName);
                    }
                    ps.setString(1, args[0]);
                    ps.setInt(2, (int) player.getLocation().getX());
                    ps.setInt(3, (int) player.getLocation().getY());
                    ps.setInt(4, (int) player.getLocation().getZ());
                    ps.setFloat(5, player.getLocation().getPitch());
                    ps.setFloat(6, player.getLocation().getYaw());
                    ps.setString(7, player.getLocation().getWorld().getName());

                    ps.setString(1, args[0]);

                    ps.executeUpdate();
                    player.sendMessage(cmdprefix + "§fCreated warp " + cmdemph + args[0] + "&f at your location");

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;
            case "delwarp":
                if (!(player.hasPermission("snw.warp.del"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /delwarp <warp name>");
                    return true;
                }

                try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM Warps WHERE WarpName = ?")){
                    ps.setString(1, args[0]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                break;

            case "warpdisplay":
            case "wdisplay":
                if (!(player.hasPermission("snw.warp.display"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }                
                player.sendMessage(cmdprefix + "§fTell cyto to fix this -cyto");
                break;

            case "w":
            case "warp":
                ArrayList<Warp> warps = WarpsDataHandler.getWarpData();
                

                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /warp <warp name>");
                    return true;
                }

                return warp(player, warps, args);


            case "reloadwarp":
            case "reloadwarps":
                if (!(player.hasPermission("snw.warp.reload"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                WarpsDataHandler.updateWarpData();
                
                player.sendMessage(cmdprefix + "§fReloaded warps.");
                break;

            case "setalias":
                if (!(player.hasPermission("snw.warp.setalias"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length < 2
                        || ParadisuMain.getWarpConfig().getConfigurationSection(args[0].toLowerCase()) == null) {
                    player.sendMessage(cmdprefix + "§fUsage: /setalias <warp> <alias>");
                    return true;
                }
                ConfigurationSection al = ParadisuMain.getWarpConfig().getConfigurationSection("aliases");
                al.set(args[1].toLowerCase(), args[0].toLowerCase());
                ParadisuMain.saveWarpConfig();
                player.sendMessage(
                        cmdprefix + cmdemph + args[1] + "§f is now a warp alias for " + cmdemph + args[0] + "§f!");
                break;

            case "delalias":
                if (!(player.hasPermission("snw.warp.delalias"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length < 2
                        || ParadisuMain.getWarpConfig().getConfigurationSection(args[0].toLowerCase()) == null
                        || ParadisuMain.getWarpConfig().getString("aliases." + args[1].toLowerCase()) == null) {
                    player.sendMessage(cmdprefix + "§fUsage: use /delalias <warp> <alias>");
                    return true;
                }
                ParadisuMain.getWarpConfig().getConfigurationSection("aliases").set(args[1].toLowerCase(), null);
                ParadisuMain.saveWarpConfig();
                player.sendMessage(cmdprefix + "§fDeleted warp alias " + cmdemph + args[1]);
                break;

            case "warps":
                Set<String> s = ParadisuMain.getWarpConfig().getKeys(false);
                s.remove("aliases");
                String mes = cmdprefix + "§fCurrent Warps:" + cmdemph;
                FileConfiguration warplist = ParadisuMain.getWarpConfig();
                for (String i : s) {
                    // player.sendMessage(mes);
                    if (warplist.getConfigurationSection(i).getString("display") != null) {
                        mes = mes.concat(
                                "\n" + "§f- " + cmdemph + warplist.getConfigurationSection(i).getString("display"));
                    } else {
                        i = i.substring(0, 1).toUpperCase() + i.substring(1);
                        mes = mes.concat("\n" + "§f- " + cmdemph + i);
                    }
                }
                player.sendMessage(mes);
                break;

            // String wstring = d.getName();
            // wstring = wstring.substring(0,1).toUpperCase() + wstring.substring(1);

        }
        return false;
    }


    private boolean warp(Player player, ArrayList<Warp> warps, String[] args){
        Warp w = findWarp(warps, args);
        if(w == null){
             player.sendMessage(cmdprefix + "§fWarp " + cmdemph + args[0] + "§f does not exist.");
             return false;
        }
        int x = w.getXPos();
        int y = w.getYPos();
        int z = w.getZPos();
        float pitch = (float) w.getPitch();
        float yaw = (float) w.getYaw();
        String world = w.getWorld();
        String permission = w.getPermission();
        String displayName = w.getDisplayName();
        String warpName = w.getName();

        if (!player.hasPermission(permission)) {
            player.sendMessage(cmdprefix + "§fYou do not have permission to use this warp.");
            return false;
        }

        Location l = player.getLocation();

        l.setX(x);
        l.setY(y);
        l.setZ(z);
        l.setPitch(pitch);
        l.setYaw(yaw);
        if(Bukkit.getServer().getWorld(world) != null) {
            l.setWorld(Bukkit.getServer().getWorld(world));
        }


        String wstring = displayName == null ? warpName : displayName;


        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
        player.teleport(l);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 29);
        player.sendMessage(cmdprefix + "§fWelcome to " + cmdemph + wstring + "§f!");
        return true;
        
    }


    private Warp findWarp(ArrayList<Warp> warps, String[] args){
        for(Warp w : warps){
            if(w.getName().equalsIgnoreCase(args[0])){
                return w;
            }
            for(String s : w.getAliases()){
                if(s.equalsIgnoreCase(args[0])){
                    return w;
                }
            }
        }




        return null;
    }
}
