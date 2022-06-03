package net.paradisu.ParadisuPlugin.warps;

import net.paradisu.ParadisuPlugin.ParadisuMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportationCommands implements CommandExecutor {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;


        switch (cmd.getName().toLowerCase()) {

            case "tphere":
                if (player.hasPermission("snw.tphere") || player.hasPermission("snw.tp.*")) {

                    if (args.length == 1) {
                        try {
                            Player target = Bukkit.getPlayerExact((args[0]));
                            target.teleport((player.getLocation()));
                            player.sendMessage(cmdprefix + "§fSuccessfully teleported " + cmdemph + target.getName() + " §fto you.");
                        } catch (NullPointerException e) {
                            player.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
                        }
                    } else {
                        player.sendMessage(cmdprefix + "§fPlease do " + cmdemph + "/tphere §fUser");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;

            case "tp":
                if (player.hasPermission("snw.tp") || player.hasPermission("snw.tp.*")) {
                    if (args.length < 1) {
                        player.sendMessage(cmdprefix + "§fPlease do §3/tp User §f§oor");
                        player.sendMessage(cmdprefix + "§fPlease do §3/tp User User");

                        // TP TO A SINGLE PLAYER
                    } else if (args.length == 1) {
                        Player target = Bukkit.getPlayerExact((args[0]));

                        try {
                            player.teleport((target.getLocation()));
                            player.sendMessage(cmdprefix + "§fSuccessfully teleported to " + cmdemph + args[0] + "§f.");
                        } catch (NullPointerException e) {
                            player.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
                        }

                        // TP ANOTHER PLAYER TO ANOTHER PLAYER
                    }
                    if (args.length == 2) {
                        Player PlayerToSend = Bukkit.getPlayerExact(args[0]);
                        Player target = Bukkit.getPlayerExact(args[1]);

                        try {
                            PlayerToSend.teleport(target.getLocation());
                            player.sendMessage(cmdprefix + "§fSuccessfully teleported " + cmdemph + PlayerToSend.getName() + " §fto " + cmdemph + target.getName() + "§f.");
                        } catch (NullPointerException e) {
                            player.sendMessage(cmdprefix + "§fOne of these players do not exist or are offline.");
                        }
                        // TOO MANY ARGS
                    } else if (args.length > 2) {
                        player.sendMessage(cmdprefix + "§fPlease do `/tp (player)` OR `/tp (player1) (player2)`");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;

            case "cordstp":
                if (player.hasPermission("snw.ctp") || player.hasPermission("snw.tp.*")) {

                    if (args.length == 3) {

                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);

                            Location location = new Location(player.getWorld(), x, y, z);
                            player.teleport(location);
                            player.sendMessage(cmdprefix + "§fWe teleported you to the cords: " + cmdemph + x + "§f, " + cmdemph + y + "§f, " + cmdemph + z + "§f.");

                        } catch (NullPointerException e) {
                            player.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
                        }

                    } else if (args.length == 4) {

                        try {
                            double x = Double.parseDouble(args[0]);
                            double y = Double.parseDouble(args[1]);
                            double z = Double.parseDouble(args[2]);
                            Player target = Bukkit.getPlayerExact(args[3]);
                            String tname = target.getName();


                            Location location = new Location(target.getWorld(), x, y, z);
                            target.teleport(location);
                            player.sendMessage(cmdprefix + "§fWe teleported " + cmdemph + tname + "§f to the cords: " + cmdemph + x + "§f, " + cmdemph + y + "§f, " + cmdemph + z + "§f.");
                            target.sendMessage(cmdprefix + "§fWe teleported you to the cords: " + cmdemph + x + "§f, " + cmdemph + y + "§f, " + cmdemph + z + "§f.");

                        } catch (NullPointerException e) {
                            player.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
                        }

                    } else {
                        player.sendMessage(noargsmsg);
                    }

                }
                return true;









            default:
                return false;
            //complain
        }
    }
}

