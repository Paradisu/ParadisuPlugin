package net.paradisu.ParadisuPlugin.items.invs;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import net.paradisu.ParadisuPlugin.ParadisuMain;
import net.paradisu.ParadisuPlugin.items.common.menu.BlankItem;
import net.paradisu.ParadisuPlugin.items.models.ModelItemManager;
import net.paradisu.ParadisuPlugin.util.InventoryGUI;

public class TrashCan implements InventoryGUI{

    String cmdprefix;
    String cmdemph;

    public TrashCan(){
        cmdprefix = ParadisuMain.CommandPrefix();
        cmdemph = ParadisuMain.CommandEmph();
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 36, "§3✮ §dTRASHCAN §3✮");

        for(int i=27; i<36; i++)
            inv.setItem(i, new BlankItem());

        inv.setItem(35, ModelItemManager.createPropModel(24));
        return inv;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if(e.getSlot() == 35) e.getWhoClicked().closeInventory();
        else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(false);
    }
}
