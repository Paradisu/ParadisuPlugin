package me.jakedadream.ParadisuPlugin.items.invs;

import me.jakedadream.ParadisuPlugin.items.common.menu.BlankItem;
import me.jakedadream.ParadisuPlugin.items.models.ModelItemManager;
import me.jakedadream.ParadisuPlugin.util.InventoryGUI;
import me.jakedadream.ParadisuPlugin.ParadisuMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class TrashCan implements InventoryGUI{

    String cmdprefix;
    String cmdemph;

    public TrashCan(){
        cmdprefix = ParadisuMain.CommandPrefix();
        cmdemph = ParadisuMain.CommandEmph();
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(null, 36, "§3✮ §dTRASHCAN §3✮");

        for(int i=27; i<36; i++)
            inv.setItem(i, new BlankItem());

        inv.setItem(35, ModelItemManager.createPropModel(24));
        return inv;
    }

    @Override
    public void onGUIClick(Player whoClicked, int slot, ItemStack clickedItem) {
        
        
    }
}
