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


    public static void InvseeInv(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§3§l" + "'s §finventory"); //4 Rows



        player.openInventory(inv);
    } // yuh

}