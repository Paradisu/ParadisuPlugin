package net.paradisu.paradisuplugin.bukkit.items.common.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlankItem extends ItemStack{
    public BlankItem(){
        super(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName("§0");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.setItemMeta(meta);
    }
}
