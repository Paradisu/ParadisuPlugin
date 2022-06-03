package net.paradisu.ParadisuPlugin.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface InventoryGUI extends InventoryHolder{
    public void onGUIClick(Player whoClicked, int slot, ItemStack clickedItem);
}
