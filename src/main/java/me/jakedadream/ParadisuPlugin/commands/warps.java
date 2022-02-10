package me.jakedadream.ParadisuPlugin.commands;

import me.jakedadream.ParadisuPlugin.paradisumain;
import me.jakedadream.ParadisuPlugin.paradisu_mysql.DBConnections;
import me.jakedadream.ParadisuPlugin.paradisu_mysql.WarpsDataHandler;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class warps implements CommandExecutor {

    private Plugin plugin;
    private FileConfiguration conf;
    private File file;

    private final String fileName = "warps.yml";

    

    private static Connection connection;

    public warps() {
        connection = DBConnections.ParadisuSQLCon();
        
    }

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
        

        switch (cmd.getName().toLowerCase()) {

            case "setwarp":

                // PreparedStatement new_player_ps = connection.prepareStatement("INSERT IGNORE
                // INTO PlayerData (UUID, NAME) VALUES (?,?)"); //
                if (!(player.hasPermission("snw.warp.set"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }

                try {
                    if (args.length == 0) {
                        player.sendMessage(ChatColor.RED + "Usage: /setwarp <name>");
                        return true;
                    }
                    PreparedStatement ps = connection.prepareStatement(
                            "INSER IGNORE INTO Warps (WarpName, X_pos, Y_pos, Z_pos, Pitch, Yaw, World, Permission, DisplayName) VALUES (?,?,?,?,?,?,?,?,?)");
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
            /*
             * Location loc = player.getLocation();
             * 
             * paradisumain.getWarpConfig().createSection(args[0].toLowerCase());
             * ConfigurationSection cs =
             * paradisumain.getWarpConfig().getConfigurationSection(args[0].toLowerCase());
             * 
             * cs.set("X", loc.getX());
             * cs.set("Y", loc.getY());
             * cs.set("Z", loc.getZ());
             * cs.set("Yaw", loc.getYaw());
             * cs.set("Pitch", loc.getPitch());
             * cs.set("World", loc.getWorld().getName());
             * cs.set("Permission", "snw.warp.default");
             * 
             * paradisumain.saveWarpConfig();
             * player.sendMessage(cmdprefix + "§fCreated warp " + cmdemph + args[0] +
             * "&f at your location");
             * break;
             */
            case "delwarp":
                if (!(player.hasPermission("snw.warp.del"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /delwarp <warp name>");
                    return true;
                }

                try {
                    PreparedStatement ps = connection
                            .prepareStatement("DELTE FROM Warps WHERE WarpName = ? VALUES (?)");
                    ps.setString(1, args[0]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // paradisumain.getWarpConfig().set(args[0], null);
                // paradisumain.saveWarpConfig();

                // String delname = args[0];
                // delname = delname.substring(0,1).toUpperCase() + delname.substring(1);

                // player.sendMessage(cmdprefix + "§fDeleted warp " + cmdemph + delname +
                // "§f.");
                break;

            case "warpdisplay":
            case "wdisplay":
                if (!(player.hasPermission("snw.warp.display"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                // String displayname = "";
                // // String sourcewarp = args[0];
                // for (int i = 0; i < args.length; i++){
                // if (i == 0) continue;
                // if (i == args.length - 1){
                // displayname += args[i];
                // continue;
                // }
                // displayname += args[i] + " ";
                // }
                // paradisumain.getWarpConfig().getConfigurationSection(args[0].toLowerCase()).set("display",
                // displayname);
                // paradisumain.saveWarpConfig();
                player.sendMessage(cmdprefix + "§fTell cyto to fix this -cyto");
                break;

            case "w":
            case "warp":
                
                
                ResultSet rs = WarpsDataHandler.getWarpData();
                

                if (args.length == 0) {
                    player.sendMessage(cmdprefix + "§fUsage: /warp <warp name>");
                    return true;
                }

                try {
                    
                    //player.sendMessage("cursor at: " + rs.getString("WarpName"));
                    rs.beforeFirst();
                    boolean warpExists = false;
                    while (rs.next()) {
                        if (rs.getString("WarpName").equalsIgnoreCase(args[0])){
                            warpExists = true;  
                            break;
                        }
                    }
                    
                    
                    if(!warpExists){
                         player.sendMessage(cmdprefix + "§fWarp " + cmdemph + args[0] + "§f does not exist.");
                         return true;
                    }
                    int x = rs.getInt("X_Pos");
                    int y = rs.getInt("Y_Pos");
                    int z = rs.getInt("Z_Pos");
                    float pitch = rs.getFloat("Pitch");
                    float yaw = rs.getFloat("Yaw");
                    String world = rs.getString("World");
                    String permission = rs.getString("Permission");
                    String displayName = rs.getString("DisplayName");
                    String warpName = rs.getString("WarpName");
                    if (!player.hasPermission(permission)) {
                        player.sendMessage(cmdprefix + "§fYou do not have permission to use this warp.");
                        return true;
                    }

                    Location l = player.getLocation();

                    // player.sendMessage(String.valueOf(d.getDouble("X")));

                    l.setX(x);
                    l.setY(y);
                    l.setZ(z);
                    l.setPitch(pitch);
                    l.setYaw(yaw);
                    if(Bukkit.getServer().getWorld(world) != null) {
                        l.setWorld(Bukkit.getServer().getWorld(world));
                    }

                    //

                    String wstring;
                    if (displayName == null) {
                        wstring = warpName;
                    } else {
                        wstring = displayName;
                        wstring = wstring.substring(0, 1).toUpperCase() + wstring.substring(1);
                    }

                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                    player.teleport(l);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 29);
                    player.sendMessage(cmdprefix + "§fWelcome to " + cmdemph + wstring + "§f!");

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // if()

                // ConfigurationSection d =
                // paradisumain.getWarpConfig().getConfigurationSection(args[0].toLowerCase());
                // if (d == null){
                // if (paradisumain.getWarpConfig().getString("aliases." +
                // args[0].toLowerCase()) != null) {
                // String w = paradisumain.getWarpConfig().getString("aliases." +
                // args[0].toLowerCase());
                // d = paradisumain.getWarpConfig().getConfigurationSection(w);
                // } else {
                // player.sendMessage(cmdprefix + "§fThat warp doesn't exist.");
                // return true;
                // }
                // }

                // def shouldn't have to set this to player loc but idk it didn't like it when i
                // left it null
                break;

            case "reloadwarp":
            case "reloadwarps":
                if (!(player.hasPermission("snw.warp.reload"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }

                // PreparedStatement ps =

                // paradisumain.reloadWarpConfig();
                player.sendMessage(cmdprefix + "§fReloaded warps.");
                break;

            case "setalias":
                if (!(player.hasPermission("snw.warp.setalias"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length < 2
                        || paradisumain.getWarpConfig().getConfigurationSection(args[0].toLowerCase()) == null) {
                    player.sendMessage(cmdprefix + "§fUsage: /setalias <warp> <alias>");
                    return true;
                }
                ConfigurationSection al = paradisumain.getWarpConfig().getConfigurationSection("aliases");
                al.set(args[1].toLowerCase(), args[0].toLowerCase());
                paradisumain.saveWarpConfig();
                player.sendMessage(
                        cmdprefix + cmdemph + args[1] + "§f is now a warp alias for " + cmdemph + args[0] + "§f!");
                break;

            case "delalias":
                if (!(player.hasPermission("snw.warp.delalias"))) {
                    player.sendMessage(nopermsmsg);
                    return true;
                }
                if (args.length < 2
                        || paradisumain.getWarpConfig().getConfigurationSection(args[0].toLowerCase()) == null
                        || paradisumain.getWarpConfig().getString("aliases." + args[1].toLowerCase()) == null) {
                    player.sendMessage(cmdprefix + "§fUsage: use /delalias <warp> <alias>");
                    return true;
                }
                paradisumain.getWarpConfig().getConfigurationSection("aliases").set(args[1].toLowerCase(), null);
                paradisumain.saveWarpConfig();
                player.sendMessage(cmdprefix + "§fDeleted warp alias " + cmdemph + args[1]);
                break;

            case "warps":
                Set<String> s = paradisumain.getWarpConfig().getKeys(false);
                s.remove("aliases");
                String mes = cmdprefix + "§fCurrent Warps:" + cmdemph;
                FileConfiguration warplist = paradisumain.getWarpConfig();
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
}
