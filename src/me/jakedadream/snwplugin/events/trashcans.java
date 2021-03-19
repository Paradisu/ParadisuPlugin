package me.jakedadream.snwplugin.events;

package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.items.ItemManager;
import me.jakedadream.snwplugin.snwplugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import sun.plugin2.main.server.Plugin;


public class trashcans implements InventoryHolder {


    private Plugin plugin = snwplugin.getPlugin(snwplugin.class)

    public void TrashCanInv(Player player) {
        Inventory inv = Bukkit.createInventory(this, 36, "§3§lWaste Bin"); //4 Rows
        ItemManager createAcceptButton = new ItemManager()
        ItemManager createBlankButton = new ItemManager()
        ItemManager createDenyButton = new ItemManager()

        inv.setItem(28, createBlankButton);
        inv.setItem(29, createBlankButton);
        inv.setItem(30, createBlankButton);
        inv.setItem(31, createBlankButton);
        inv.setItem(32, createBlankButton);
        inv.setItem(33, createBlankButton);
        inv.setItem(34, createBlankButton);
        inv.setItem(35, createDenyButton);
        inv.setItem(36, createAcceptButton);

        player.openInventory(inv);
    }
}

        // cum