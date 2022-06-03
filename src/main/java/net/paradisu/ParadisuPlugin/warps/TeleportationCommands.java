package net.paradisu.ParadisuPlugin.warps;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.paradisu.ParadisuPlugin.ParadisuMain;

public class TeleportationCommands {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();


    @CommandPermission("paradisu.tphere")
    @CommandMethod("tphere|atphere <player>")
    public void tphere(CommandSender sender,
        @Argument("player") Player player
    ){
        Player p = (Player) sender;
        player.teleport((p.getLocation()));
        p.sendMessage(cmdprefix + "§fSuccessfully teleported " + cmdemph + player.getName() + " §fto you.");
    }

    @CommandPermission("paradisu.tp")
    @CommandMethod("tp|atp <playerOne> [playerTwo]")
    public void tp(CommandSender sender,
        @Argument("playerOne") Player playerOne,
        @Argument("playerTwo") Player playerTwo
    ){
        Player p = (Player) sender;
        if(playerTwo == null){
            p.teleport(playerOne.getLocation());
            p.sendMessage(cmdprefix + "§fSuccessfully teleported to " + cmdemph + playerOne.getName() + "§f.");              
        } else {
            playerOne.teleport(playerTwo.getLocation());
            playerTwo.teleport(playerOne.getLocation());
            p.sendMessage(cmdprefix + "§fSuccessfully teleported " + cmdemph + playerOne.getName() + " §fto " + cmdemph + playerTwo.getName() + "§f.");
        }
    }

    @CommandPermission("paradisu.ctp")
    @CommandMethod("ctp|coordstp <x> <y> <z>")
    public void coordsTp(CommandSender sender,
        @Argument("x") double x,
        @Argument("y") double y,
        @Argument("z") double z
    ){
        Player p = (Player) sender;
        Location loc = new Location(p.getWorld(), x, y, z);
        p.teleport(loc);
        p.sendMessage(cmdprefix + "§fSuccessfully teleported to " + cmdemph + loc.getX() + " " + loc.getY() + " " + loc.getZ() + "§f.");
    }
}

