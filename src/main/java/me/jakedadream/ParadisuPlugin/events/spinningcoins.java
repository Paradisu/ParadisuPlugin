package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class spinningcoins extends BukkitRunnable {



    public void run(){

    }

    public static void spineffect() {

        //System.out.println("in runnable");
        Bukkit.getWorlds();
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {

                //if (e.getType() == EntityType.ARMOR_STAND) {

                   // ArmorStand as = (ArmorStand) e;
                    if(e.getCustomName() == null) return;


                    //vvvv @ JAKE THIS IS WHAT ISN'T WORKING
                    if (e.getCustomName().equalsIgnoreCase("CoinSpin")) {

                        Location asloc = e.getLocation();
                        Float yawvar = 6.0F;
                        asloc.setYaw(e.getLocation().getYaw() + yawvar);
                        e.teleport(asloc);


                   } else return;
                //} else return;
            }
        }
    }
    // end
}
