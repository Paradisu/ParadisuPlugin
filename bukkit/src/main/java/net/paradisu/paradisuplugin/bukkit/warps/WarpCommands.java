package net.paradisu.paradisuplugin.bukkit.warps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import cloud.commandframework.annotations.specifier.Greedy;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class WarpCommands {

    private static DataSource dataSource;

    public WarpCommands() {
        dataSource = ParadisuMain.getDBCon();

    }

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();

    @CommandPermission("paradisu.warp.default")
    @CommandMethod("warp|w <warpName>")
    public void warp(CommandSender sender, @Argument("warpName") String warpName) {
        Player p = (Player) sender;
        ArrayList<Warp> warps = WarpsDataHandler.getWarpData();
        playerWarp(p, warps, warpName);
    }

    @CommandPermission("paradisu.warp.default")
    @CommandMethod("spawn")
    public void spawn(CommandSender sender){
        Player p = (Player) sender;
        ArrayList<Warp> warps = WarpsDataHandler.getWarpData();
        playerWarp(p, warps, "spawn");
    }

    @CommandPermission("paradisu.warp.admin")
    @ProxiedBy("setwarp")
    @CommandMethod("warp|w add|set <warpName> [displayName]")
    @CommandDescription("Adds a warp to the database")
    public void addWarp(CommandSender sender,
        @Argument("warpName") String warpName,
        @Argument("displayName") @Greedy String displayName
    ) {
        Player player = (Player) sender;
        
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT IGNORE INTO Warps (WarpName, X_pos, Y_pos, Z_pos, Pitch, Yaw, World, Permission, DisplayName) VALUES (?,?,?,?,?,?,?,?,?)")) {

            
            ps.setString(9, displayName);
            
            ps.setString(1, warpName);
            ps.setInt(2, (int) player.getLocation().getX());
            ps.setInt(3, (int) player.getLocation().getY());
            ps.setInt(4, (int) player.getLocation().getZ());
            ps.setFloat(5, player.getLocation().getPitch());
            ps.setFloat(6, player.getLocation().getYaw());
            ps.setString(7, player.getLocation().getWorld().getName());
            ps.setString(8, "paradisu.warp.default");

            ps.executeUpdate();
            player.sendMessage(cmdprefix + "§fCreated warp " + cmdemph + warpName + "&f at your location");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        WarpsDataHandler.updateWarpData();
    }

    @CommandPermission("paradisu.warp.admin")
    @ProxiedBy("delwarp")
    @CommandMethod("warp|w delete|remove <warpName>")
    @CommandDescription("Deletes a warp from the database")
    public void delWarp(CommandSender sender,
        @Argument("warpName") String warpName
    ){
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Warps WHERE WarpName = ?")) {

            ps.setString(1, warpName);
            ps.executeUpdate();
            sender.sendMessage(cmdprefix + "§fDeleted warp " + cmdemph + warpName);
        } catch (SQLException e) {
            sender.sendMessage("§cError: " + e.getMessage());
        }
        WarpsDataHandler.updateWarpData();
    }

    @CommandPermission("paradisu.warp.default")
    @CommandDescription("Displays a list of all warps")
    @ProxiedBy("warps")
    @CommandMethod("warp|w list")
    public void listWarps(CommandSender sender){
        ArrayList<Warp> warps = WarpsDataHandler.getWarpData();
        String msg = "§fWarps:";
        for(Warp warp : warps){
            String name = warp.getDisplayName()==null ? warp.getName() : warp.getDisplayName();
            msg += "\n- " + name;
        }
        sender.sendMessage(cmdprefix + msg);
    }

    @CommandPermission("paradisu.warp.admin")
    @CommandDescription("Reloads the warp data")
    @ProxiedBy("reloadwarps|relwarps")
    @CommandMethod("warp|w reload")
    public void reloadWarps(CommandSender sender){
        WarpsDataHandler.updateWarpData();
        sender.sendMessage(cmdprefix + "§fReloaded warps");
    }


    //util methods
    private boolean playerWarp(Player player, ArrayList<Warp> warps, String warpName) {
        String[] args = new String[1];
        args[0] = warpName;
        return playerWarp(player, warps, args);
    }

    private boolean playerWarp(Player player, ArrayList<Warp> warps, String[] args) {
        Warp w = findWarp(warps, args);
        if (w == null) {
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
        if (Bukkit.getServer().getWorld(world) != null) {
            l.setWorld(Bukkit.getServer().getWorld(world));
        }

        String wstring = displayName == null ? warpName : displayName;

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
        player.teleport(l);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 29);
        player.sendMessage(cmdprefix + "§fWelcome to " + cmdemph + wstring + "§f!");
        return true;

    }

    private Warp findWarp(ArrayList<Warp> warps, String[] args) {
        for (Warp w : warps) {
            if (w.getName().equalsIgnoreCase(args[0])) {
                return w;
            }
            for (String s : w.getAliases()) {
                if (s.equalsIgnoreCase(args[0])) {
                    return w;
                }
            }
        }

        return null;
    }
}
