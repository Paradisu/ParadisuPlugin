package me.jakedadream.snwplugin.events;

import com.sun.deploy.security.ValidationState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import sun.plugin2.main.server.Plugin;

public class entityedits implements Listener {



        public void run() {
        Bukkit.getWorlds();
        for (World w : Bukkit.getWorlds()) {
            for (Entity entity : w.getEntities()) {

//                =======================
//                ENTITY NO AI
//                =======================
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).getActivePotionEffects();
                    if (((LivingEntity) entity).hasPotionEffect(PotionEffectType.LUCK)) {
                        ((LivingEntity) entity).setAI(true);
                    } else {
                        ((LivingEntity) entity).setAI(false);
                    }
                }


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

    /*
   (new BukkitRunnable() {


   ).runTaskTimer((Plugin)this, 0L, 1L);   */
}

