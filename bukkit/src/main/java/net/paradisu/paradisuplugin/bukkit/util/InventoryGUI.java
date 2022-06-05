package net.paradisu.paradisuplugin.bukkit.util;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface InventoryGUI extends InventoryHolder{
    public void onClick(InventoryClickEvent e);
}
