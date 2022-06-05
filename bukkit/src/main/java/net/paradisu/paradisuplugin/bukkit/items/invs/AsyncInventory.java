package net.paradisu.paradisuplugin.bukkit.items.invs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class AsyncInventory {

    JavaPlugin paradisuPlugin = ParadisuMain.getPlugin(ParadisuMain.class);

   //public BukkitTask openInventory(Player player, int slots, String name) {
    public BukkitTask openInventory(Player player, Inventory AsyncInv) {
        return Bukkit.getScheduler().runTaskAsynchronously(paradisuPlugin, new Runnable() {
            @Override
            public void run() {
              //Inventory asyncInventory = Bukkit.createInventory(player, slots, name);
                player.openInventory(AsyncInv);
            }
        });
    }
}
