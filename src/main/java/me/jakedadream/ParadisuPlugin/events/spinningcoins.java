package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public class spinningcoins {

    public static void spineffect() {

        Bukkit.getWorlds();
        for (World w : Bukkit.getWorlds()) {
            for (Entity e : w.getEntities()) {

                if (e instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) e;

                    if (as.getName().equalsIgnoreCase("CoinSpin")) {

                        Location asloc = as.getLocation();
                        Float yawvar = 6.0F;
                        asloc.setYaw(as.getLocation().getYaw() + yawvar);
                        as.teleport(asloc);

                    } else return;
                } else return;
            }
        }
    }
    // end
}
