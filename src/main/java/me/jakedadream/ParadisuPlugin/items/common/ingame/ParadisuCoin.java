package me.jakedadream.ParadisuPlugin.items.common.ingame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ParadisuCoin extends ItemStack {

    public ParadisuCoin(int amount){
        super(Material.GOLD_NUGGET, amount);
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName("§e§lGOLD COIN");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.setItemMeta(meta);
    }

    public ParadisuCoin() {
        this(1);
    }
}
