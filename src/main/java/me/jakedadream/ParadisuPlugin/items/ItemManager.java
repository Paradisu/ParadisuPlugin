package me.jakedadream.ParadisuPlugin.items;

import me.jakedadream.ParadisuPlugin.ParadisuMain;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();


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

        public static ItemStack BlankItemSlot() {
            ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§0");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            return item;
        }

        public static ItemStack ItemNoExist() {
            ItemStack item = new ItemStack(Material.DIAMOND_AXE, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§4§l❌ §c§lEmpty Slot §4§l❌");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setCustomModelData(76);
            item.setItemMeta(meta);
            return item;
        }
}
