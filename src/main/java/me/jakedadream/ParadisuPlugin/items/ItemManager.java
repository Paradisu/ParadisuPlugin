package me.jakedadream.ParadisuPlugin.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

        public static ItemStack createCoin() {
            ItemStack item = new ItemStack(Material.GOLD_NUGGET, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§lGOLD COIN");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
    }

        public static ItemStack createStarCoin() {
            ItemStack item = new ItemStack(Material.IRON_NUGGET, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§6✮ §f§k| §eSTAR COIN §f§k| §6✮");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
    }

 // end

}
