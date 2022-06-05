package net.paradisu.paradisuplugin.bukkit.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class SpinningCoins extends BukkitRunnable {

    public static void spineffect() {

        //System.out.println("in runnable");
        Bukkit.getWorlds();
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {


                if (e instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) e;
                    if (as.getName().equalsIgnoreCase("spin") || as.getName().contains("modelscroller")) {
                        Location loc = as.getLocation();
                        loc.setYaw(as.getLocation().getYaw() + 12.0F);
                        as.teleport(loc);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        return;
        
    }
}