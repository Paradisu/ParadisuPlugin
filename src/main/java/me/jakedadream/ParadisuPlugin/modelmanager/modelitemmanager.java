package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class modelitemmanager {

    public static ItemStack createPropModel() {
        ItemStack propmodelitem = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = propmodelitem.getItemMeta();
        //
        List<String> propmodellore = new ArrayList<>();
        //
        String cs = "uhhhhhhhhhh plz go back to this"; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ConfigurationSection gs = paradisumain.getPropModelsConfig().getConfigurationSection(cs);
        //
        meta.setCustomModelData(Integer.valueOf(gs.getName()));
        //
        meta.setDisplayName(gs.getString("displayname")); // displayname
        //s
        meta.addEnchant(gs.getString("enchantslot1"), gs.getString("enchantslot1level")); // enchant slot 1
        meta.addEnchant(gs.getString("enchantslot2"), gs.getString("enchantslot2level")); // enchant slot 2
        meta.addEnchant(gs.getString("enchantslot3"), gs.getString("enchantslot3level")); // enchant slot 3
        //
        propmodellore.add(gs.getString("lore1")); // lore line 1
        propmodellore.add(gs.getString("lore2")); // lore line 2
        propmodellore.add(gs.getString("lore3")); // lore line 3
        propmodellore.add(gs.getString("lore4")); // lore line 4
        propmodellore.add(gs.getString("lore5")); // lore line 5
        propmodellore.add(gs.getString("lore6")); // lore line 6
        propmodellore.add(gs.getString("lore7")); // lore line 7
        propmodellore.add(gs.getString("lore8")); // lore line 8
        propmodellore.add(gs.getString("lore9")); // lore line 9
        propmodellore.add(gs.getString("lore10")); // lore line 10
        //
        meta.setLore(propmodellore); // sets prop lore
        //
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(Boolean.parseBoolean(gs.getString("unbreakable"))); // unbreakable? true/false
        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideunbreakable"))); // hide unbreakable? true/false
        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideenchants"))); // hide enchants? true/false
        //

        return propmodelitem;
    }






    public static ItemStack createHatModel() {
        ItemStack hatmodelitem = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = hatmodelitem.getItemMeta();
        //
        List<String> hatmodellore = new ArrayList<>();
        //
        String cs = "uhhhhhhhhhh plz go back to this"; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ConfigurationSection gs = paradisumain.getHatModelsConfig().getConfigurationSection(cs);
        //
        meta.setCustomModelData(Integer.valueOf(gs.getName()));
        //
        meta.setDisplayName(gs.getString("displayname")); // displayname
        //s
        meta.addEnchant(gs.getString("enchantslot1"), gs.getString("enchantslot1level")); // enchant slot 1
        meta.addEnchant(gs.getString("enchantslot2"), gs.getString("enchantslot2level")); // enchant slot 2
        meta.addEnchant(gs.getString("enchantslot3"), gs.getString("enchantslot3level")); // enchant slot 3
        //
        hatmodellore.add(gs.getString("lore1")); // lore line 1
        hatmodellore.add(gs.getString("lore2")); // lore line 2
        hatmodellore.add(gs.getString("lore3")); // lore line 3
        hatmodellore.add(gs.getString("lore4")); // lore line 4
        hatmodellore.add(gs.getString("lore5")); // lore line 5
        hatmodellore.add(gs.getString("lore6")); // lore line 6
        hatmodellore.add(gs.getString("lore7")); // lore line 7
        hatmodellore.add(gs.getString("lore8")); // lore line 8
        hatmodellore.add(gs.getString("lore9")); // lore line 9
        hatmodellore.add(gs.getString("lore10")); // lore line 10
        //
        meta.setLore(hatmodellore); // sets prop lore
        //
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(Boolean.parseBoolean(gs.getString("unbreakable"))); // unbreakable? true/false
        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideunbreakable"))); // hide unbreakable? true/false
        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideenchants"))); // hide enchants? true/false
        //

        return hatmodelitem;
    }



}
