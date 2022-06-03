package net.paradisu.ParadisuPlugin.items.common.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoItem extends ItemStack{
    public NoItem(){
        super(Material.DIAMOND_AXE, 1);
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName("§4§l❌ §c§lEmpty Slot §4§l❌");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(76);
        this.setItemMeta(meta);
    }

    public NoItem(String slotName){
        this();
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(meta.getDisplayName() + slotName);
        this.setItemMeta(meta);
    }
}
