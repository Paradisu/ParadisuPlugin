package me.jakedadream.ParadisuPlugin.wrappers;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class announcementwrapper {


    public static void permannoucne(String permission, String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission(permission)) {

                    p.sendMessage("\uE013 §f| \uE013 \n" + "                 §x§f§d§d§0§2§3Announcement \n \n " + 
                    "§f" + message + "\n \n" +
                    "\uE013 §f| \uE013"
                    );

                }
            }
        }
    }

    public static void adminannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("snw.*")) {

                    p.sendMessage("\uE013 §f| \uE013 \n \n" +
                    "§4Admins: §c§o" + message + "\n \n" +
                    "\uE013 §f| \uE013"
                    );

                }
            }
        }
    }

    public static void staffannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("group.staff")) {

                    p.sendMessage("\uE013 §f| \uE013 \n \n" +
                    "§3Staff: §b§o" + message + "\n \n" +
                    "\uE013 §f| \uE013"
                    );

                }
            }
        }
    }

    public static void supportersannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("meta.rank.supporter") || p.hasPermission("group.staff")) {

                    p.sendMessage("\uE013 §f| \uE013 \n" + "                    §d \n \n " + 
                    "§f" + message + "\n \n" +
                    "\uE013 §f| \uE013"
                    );

                }
            }
        }
    }

    public static void everyoneannoucne(String message) {

        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {

                p.sendMessage("\uE013 §f| \uE013 \n" + "                 §x§f§d§d§0§2§3Announcement \n \n " + 
                "§f" + message + "\n \n" +
                "\uE013 §f| \uE013"
                );

            }
        }
    }

    public static void staffchat(String group, Player player, String message) {

    }
}
