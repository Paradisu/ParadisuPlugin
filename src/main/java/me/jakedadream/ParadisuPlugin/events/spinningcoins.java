package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class spinningcoins extends BukkitRunnable {


    public void run() {

    }

    public static void spineffect() {


        //System.out.println("in runnable");
        Bukkit.getWorlds();
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {


                if (e instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) e;
                    if (as.getName().equalsIgnoreCase("spin")) {
                        Location loc = as.getLocation();
                        loc.setYaw(as.getLocation().getYaw() + 6.0F);
                        as.teleport(loc);
                    }
                }

            }
        }
    }
}