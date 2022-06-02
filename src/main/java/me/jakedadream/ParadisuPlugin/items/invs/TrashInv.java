package me.jakedadream.ParadisuPlugin.items.invs;

import me.jakedadream.ParadisuPlugin.items.GenItemManager;
import me.jakedadream.ParadisuPlugin.items.models.ModelItemManager;
import me.jakedadream.ParadisuPlugin.ParadisuMain;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class TrashInv {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();


    public static Inventory TrashcanGUI() {

        Inventory inv = Bukkit.createInventory(null, 36, "§3✮ §dTRASHCAN §3✮");


        inv.setItem(27, GenItemManager.BlankItemSlot());
        inv.setItem(28, GenItemManager.BlankItemSlot());
        inv.setItem(29, GenItemManager.BlankItemSlot());
        inv.setItem(30, GenItemManager.BlankItemSlot());
        inv.setItem(31, GenItemManager.BlankItemSlot());
        inv.setItem(32, GenItemManager.BlankItemSlot());
        inv.setItem(33, GenItemManager.BlankItemSlot());
        inv.setItem(34, GenItemManager.BlankItemSlot());

        inv.setItem(35, ModelItemManager.createPropModel(24));

        return inv;
    }
}
