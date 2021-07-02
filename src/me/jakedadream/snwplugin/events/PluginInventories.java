package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.jakedadream.snwplugin.items.ItemManager.*;


public class PluginInventories {

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
    } // yuh

    public static void ItemShowcase(Player player) {
        snwevents snwe = new snwevents();
        snwevents createDisplayItem = new snwevents();
        Inventory inv = Bukkit.createInventory(null, 27, "§3§l" + player.getDisplayName() + "'s §fitem!");
        ItemManager createBlankButton = new ItemManager();
        inv.setItem(0, blankbutton);
        inv.setItem(1, blankbutton);
        inv.setItem(2, blankbutton);
        inv.setItem(3, blankbutton);
        inv.setItem(4, blankbutton);
        inv.setItem(5, blankbutton);
        inv.setItem(6, blankbutton);
        inv.setItem(7, blankbutton);
        inv.setItem(8, blankbutton);
        inv.setItem(18, blankbutton);
        inv.setItem(19, blankbutton);
        inv.setItem(20, blankbutton);
        inv.setItem(21, blankbutton);
        inv.setItem(22, blankbutton);
        inv.setItem(23, blankbutton);
        inv.setItem(24, blankbutton);
        inv.setItem(25, blankbutton);
        inv.setItem(26, blankbutton);
    }
}