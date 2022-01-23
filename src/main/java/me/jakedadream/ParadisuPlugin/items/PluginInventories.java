package me.jakedadream.ParadisuPlugin.items;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.jakedadream.ParadisuPlugin.commands.snwcommands;

public class PluginInventories {

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();

    public static void TrashCanInv(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§3§lWaste Bin"); //4 Rows

     /*   inv.setItem(27, ItemManager.blankbutton);
        inv.setItem(28, ItemManager.blankbutton);
        inv.setItem(29, ItemManager.blankbutton);
        inv.setItem(30, ItemManager.blankbutton);
        inv.setItem(31, ItemManager.blankbutton);
        inv.setItem(32, ItemManager.blankbutton);
        inv.setItem(33, ItemManager.blankbutton);
        inv.setItem(34, ItemManager.denybutton);
        inv.setItem(35, ItemManager.acceptbutton);
*/
        player.openInventory(inv);
    } // yuh


}
