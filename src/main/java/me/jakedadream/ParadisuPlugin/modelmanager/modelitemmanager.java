package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.paradisumain;
import me.jakedadream.ParadisuPlugin.paradisu_mysql.DBConnections;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class modelitemmanager {

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();

    private static ResultSet propData;
    private static ResultSet hatData;

    
    public static ResultSet returnPropData(){
        if(propData == null) getPropData();
        return propData;
    }
    public static ResultSet returnHatData(){
        if(hatData == null) getHatData();
        return hatData;
    }

    public static ItemStack createPropModel(Integer modelid) {

        if(propData == null){
            getPropData();

        }

        ItemStack propmodelitem = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = propmodelitem.getItemMeta();
        //
        //List<String> propmodellore = new ArrayList<>();


        //

        //ConfigurationSection gs = paradisumain.getPropModelsConfig().getConfigurationSection(modelid.toString());
        //
        assert meta != null;

        try {
            propData.absolute(modelid);
            meta.setCustomModelData(propData.getInt("customModelData"));
            meta.setDisplayName(propData.getString("displayname"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // meta.setCustomModelData(Integer.parseInt(gs.getName()));
        // //
        // meta.setDisplayName(gs.getString("displayname")); // displayname
        // //

//        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(gs.getString("enchantslot1"))), gs.getInt("enchantslot1level"), true);
//        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(gs.getString("enchantslot2"))), gs.getInt("enchantslot2level"), true);
//        meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(gs.getString("enchantslot3"))), gs.getInt("enchantslot3level"), true);
//        meta.addEnchant(EnchantmentWrapper.getByKey(NamespacedKey.minecraft(gs.getString("enchantslot1"))), gs.getString("enchantslot1level")); // enchant slot 1
//        meta.addEnchant(gs.getString("enchantslot2"), gs.getString("enchantslot2level")); // enchant slot 2
//        meta.addEnchant(gs.getString("enchantslot3"), gs.getString("enchantslot3level")); // enchant slot 3
//

        // int lorelines = 0;
        // for (int i = 1; i < 11; i++){
        //     if (gs.getString("lore" + i) == null){
        //         break;
        //     }
        //     lorelines++;
        // }

        // for (int j = 0; j < lorelines; j++){
        //     propmodellore.add(gs.getString("lore" + Math.addExact(j, 1)));
        // }


        //
        //meta.setLore(propmodellore); // sets prop lore
        //
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true); // unbreakable? true/false
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        


        propmodelitem.setItemMeta(meta);

        return propmodelitem;
    }

    private static void getPropData() {
        Connection c = DBConnections.ParadisuSQLCon();
        try{
            PreparedStatement query = c.prepareStatement("SELECT * FROM PropModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            propData = query.executeQuery();
           
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void getHatData(){
        Connection c = DBConnections.ParadisuSQLCon();

        try{
            PreparedStatement query = c.prepareStatement("SELECT * FROM HatModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            hatData = query.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static ItemStack createHatModel(Integer modelid) {

        if(hatData == null){
            getHatData();
        }

        ItemStack hatmodelitem = new ItemStack(Material.CARVED_PUMPKIN, 1);
        ItemMeta meta = hatmodelitem.getItemMeta();
        //
        //List<String> hatmodellore = new ArrayList<>();
        //
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!




        //ConfigurationSection gs = paradisumain.getHatModelsConfig().getConfigurationSection(modelid.toString());
        //
         assert meta != null;
        // assert gs != null;

        try {
            hatData.absolute(modelid);
            meta.setCustomModelData(hatData.getInt("customModelData"));
            meta.setDisplayName(hatData.getString("displayname"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //
         // displayname
        //


        // int lorelines = 0;
        // for (int i = 1; i < 11; i++){
        //     if (gs.getString("lore" + i) == null){
        //         break;
        //     }
        //     lorelines++;
        // }

        // for (int j = 0; j < lorelines; j++){
        //     hatmodellore.add(gs.getString("lore" + Math.addExact(j, 1)));
        // }

        // //
        // meta.setLore(hatmodellore); // sets prop lore
        //
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true); // unbreakable? true/false
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        

//        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideunbreakable"))); // hide unbreakable? true/false
//        meta.addItemFlags(ItemFlag.valueOf(gs.getString("hideenchants"))); // hide enchants? true/false
        //

        hatmodelitem.setItemMeta(meta);

        return hatmodelitem;
    }
}