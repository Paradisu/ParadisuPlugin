package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

public class entityedits implements Listener {



        public void load() {
            Bukkit.getWorlds();
            for (World w : Bukkit.getWorlds()) {

                for (Entity entity : w.getEntities()) {

//                =======================
//                Coin Spin
//                =======================
                    ArmorStand as = (ArmorStand) entity;
                    if (entity == as) {
                        if (as.getName().equalsIgnoreCase("coin")) {
                            Location loc = as.getLocation();
                            loc.setYaw(as.getLocation().getYaw() + 6.0F);
                            as.teleport(loc);
                        }
                    }
                }
            }
        }
}

