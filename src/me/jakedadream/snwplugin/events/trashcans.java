package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;

import static me.jakedadream.snwplugin.items.ItemManager.*;


public class trashcans {

    public static void TrashCanInv(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§3§lWaste Bin"); //4 Rows
        ItemManager createAcceptButton = new ItemManager();
        ItemManager createBlankButton = new ItemManager();
        ItemManager createDenyButton = new ItemManager();

        inv.setItem(27, blankbutton);
        inv.setItem(28, blankbutton);
        inv.setItem(29, blankbutton);
        inv.setItem(30, blankbutton);
        inv.setItem(31, blankbutton);
        inv.setItem(32, blankbutton);
        inv.setItem(33, blankbutton);
        inv.setItem(34, denybutton);
        inv.setItem(35, acceptbutton);

        player.openInventory(inv);
    }
}