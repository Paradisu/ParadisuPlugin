package me.jakedadream.ParadisuPlugin.items;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class PluginInventories {

    public static void TrashCanInv(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§3§lWaste Bin"); //4 Rows
        ItemManager createAcceptButton = new ItemManager();
        ItemManager createBlankButton = new ItemManager();
        ItemManager createDenyButton = new ItemManager();

        inv.setItem(27, ItemManager.blankbutton);
        inv.setItem(28, ItemManager.blankbutton);
        inv.setItem(29, ItemManager.blankbutton);
        inv.setItem(30, ItemManager.blankbutton);
        inv.setItem(31, ItemManager.blankbutton);
        inv.setItem(32, ItemManager.blankbutton);
        inv.setItem(33, ItemManager.blankbutton);
        inv.setItem(34, ItemManager.denybutton);
        inv.setItem(35, ItemManager.acceptbutton);

        player.openInventory(inv);
    } // yuh




}
