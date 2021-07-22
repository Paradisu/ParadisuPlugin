package me.jakedadream.ParadisuPlugin.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class toys implements Listener {

    @EventHandler
    public void dslauncher(final PlayerInteractEvent e) {
        if (e.getAction() != null) {
            Player p = e.getPlayer();

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
                return;

            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR) {

                if (e.getItem() == null) return;

                if (e.getItem().getType() == Material.DIAMOND_AXE) {
                    if (e.getItem().getItemMeta().getCustomModelData() == 146) {

                        ItemStack item = new ItemStack(Material.DEAD_TUBE_CORAL_BLOCK, 1);
                        Snowball s = e.getPlayer().launchProjectile(Snowball.class);
                        s.setItem(item);


                        Random rd = new Random();
                        Float rpitch = rd.nextFloat();
                        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1F, rpitch);
                    }
                }
            }
        }
    }


    // Finado
}
