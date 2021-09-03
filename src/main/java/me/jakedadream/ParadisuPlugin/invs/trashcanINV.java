package me.jakedadream.ParadisuPlugin.invs;

import me.jakedadream.ParadisuPlugin.items.ItemManager;
import me.jakedadream.ParadisuPlugin.modelmanager.modelitemmanager;
import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class trashcanINV {

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();


    public static Inventory TrashcanGUI() {

        Inventory inv = Bukkit.createInventory(null, 36, "§3✮ §dTRASHCAN §3✮");


        inv.setItem(27, ItemManager.BlankItemSlot());
        inv.setItem(28, ItemManager.BlankItemSlot());
        inv.setItem(29, ItemManager.BlankItemSlot());
        inv.setItem(30, ItemManager.BlankItemSlot());
        inv.setItem(31, ItemManager.BlankItemSlot());
        inv.setItem(32, ItemManager.BlankItemSlot());
        inv.setItem(33, ItemManager.BlankItemSlot());
        inv.setItem(34, ItemManager.BlankItemSlot());

        inv.setItem(35, modelitemmanager.createPropModel(24));

        return inv;
    }
}
