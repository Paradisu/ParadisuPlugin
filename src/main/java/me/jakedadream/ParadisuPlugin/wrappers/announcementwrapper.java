package me.jakedadream.ParadisuPlugin.wrappers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class announcementwrapper {

    public static void permannoucne(String permission, String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission(permission)) {

                    p.sendMessage(message);

                }
            }
        }
    }

    public static void adminannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("snw.*")) {

                    p.sendMessage(message);

                }
            }
        }
    }

    public static void staffannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("group.staff")) {

                    p.sendMessage(message);

                }
            }
        }
    }

    public static void supportersannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                if (p.hasPermission("meta.rank.supporter") || p.hasPermission("group.staff")) {

                    p.sendMessage(message);

                }
            }
        }
    }

    public static void everyoneannoucne(String message) {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {

                    p.sendMessage(message);

            }
        }
    }



}
