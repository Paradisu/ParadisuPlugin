package me.jakedadream.ParadisuPlugin.events;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.jakedadream.ParadisuPlugin.ParadisuMain;
import me.jakedadream.ParadisuPlugin.wrappers.EventCooldowns;

public class toys implements Listener {


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String cooldownmsg = ParadisuMain.PlayerCooldownMessage();


    EventCooldowns eventCooldowns = new EventCooldowns();

    @EventHandler
    public void dslauncher(final PlayerInteractEvent e) {
        if (e.getAction() != null) {
            Player p = e.getPlayer();
            UUID uuid = p.getUniqueId();

            /*
             *      COOL DOWN TIME (MILI)
             */
            long cooldown_time = 500;
            /*
             *
             */


            if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                return;

            if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR) {

                if (e.getItem() == null) return;

                if (e.getItem().getType() == Material.DIAMOND_AXE) {
                    if (e.getItem().getItemMeta().hasCustomModelData()) {
                        if (e.getItem().getItemMeta().getCustomModelData() == 146) {
                            if (eventCooldowns.CheckPlayerOnCooldown(uuid)) {

                                if (!e.getItem().getItemMeta().hasEnchants()) {
                                    ItemStack item = new ItemStack(Material.DEAD_TUBE_CORAL_BLOCK, 1);
                                    Snowball s = e.getPlayer().launchProjectile(Snowball.class);
                                    s.setItem(item);

                                    Random rd = new Random();
                                    Float rpitch = rd.nextFloat();
                                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1F, rpitch);
                                } else {
                                    int Burst_Limit = 5;
                                    for (int i = 0; i < Burst_Limit; i++) {
                                        ItemStack item = new ItemStack(Material.DEAD_TUBE_CORAL_BLOCK, 1);
                                        Snowball s = e.getPlayer().launchProjectile(Snowball.class);
                                        s.setItem(item);
                                    }
                                    Random rd = new Random();
                                    Float rpitch = rd.nextFloat();
                                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1F, rpitch);
                                }

                                if (!(p.hasPermission("*"))) {
                                    eventCooldowns.PutPlayerOnCooldown(uuid, cooldown_time);
                                }
                            } else {
                                p.sendMessage(cooldownmsg);
                            }
                        } else {
                            return;
                            }
                    } else {
                        return;
                    }
                }
            }
        }
    }



    // Finado
}
