package net.paradisu.paradisuplugin.bukkit.items.common.ingame;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StarCoin extends ItemStack {
    public StarCoin(int amount){
        super(Material.IRON_NUGGET, amount);
        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName("§6✮ §f§k| §eSTAR COIN §f§k| §6✮");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.setItemMeta(meta);
    }

    public StarCoin(){
        this(1);
    }
}
