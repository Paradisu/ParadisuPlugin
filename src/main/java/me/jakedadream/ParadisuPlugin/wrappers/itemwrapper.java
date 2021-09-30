package me.jakedadream.ParadisuPlugin.wrappers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class itemwrapper {

    public static ItemStack createCustomItem(Material material, int amount, int custommodelid, String name, boolean hideattributes) {
        //
        ItemStack item = new ItemStack(material, amount);
        //
        ItemMeta meta = item.getItemMeta();
        //
        meta.setDisplayName(name);
        //
        meta.setCustomModelData(custommodelid);
        //
        if (hideattributes == true) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        //
        item.setItemMeta(meta);
        //
        return item;
    }


}
