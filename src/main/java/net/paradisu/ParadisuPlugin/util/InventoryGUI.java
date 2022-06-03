package net.paradisu.ParadisuPlugin.util;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface InventoryGUI extends InventoryHolder{
    public void onClick(InventoryClickEvent e);
}
