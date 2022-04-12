package me.jakedadream.ParadisuPlugin.modelmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jakedadream.ParadisuPlugin.ParadisuMain;

public class modelitemmanager {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    private static ResultSet propData;
    private static ResultSet hatData;


    private static void fetchPropData() {
        try (Connection c = ParadisuMain.getDBCon().getConnection(); PreparedStatement query = c.prepareStatement("SELECT * FROM PropModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {    
            propData = query.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void fetchHatData(){
        try (Connection c = ParadisuMain.getDBCon().getConnection(); PreparedStatement query = c.prepareStatement("SELECT * FROM HatModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) { 
            hatData = query.executeQuery(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static ItemStack createPropModel(Integer modelid) {

        if(propData == null){
            fetchPropData();
        }

        ItemStack propmodelitem = new ItemStack(Material.DIAMOND_AXE, 1);
        ItemMeta meta = propmodelitem.getItemMeta();
       
        try {
            propData.absolute(modelid);
            meta.setCustomModelData(propData.getInt("customModelData"));
            meta.setDisplayName(propData.getString("displayname"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true); 
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        propmodelitem.setItemMeta(meta);

        return propmodelitem;
    }

    

    public static ItemStack createHatModel(Integer modelid) {

        if(hatData == null){
            fetchHatData();
        }

        ItemStack hatmodelitem = new ItemStack(Material.CARVED_PUMPKIN, 1);
        ItemMeta meta = hatmodelitem.getItemMeta();

        try {
            hatData.absolute(modelid);
            meta.setCustomModelData(hatData.getInt("customModelData"));
            meta.setDisplayName(hatData.getString("displayname"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
     
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        

        hatmodelitem.setItemMeta(meta);

        return hatmodelitem;
    }

    public static ResultSet getPropData() {
        if(propData == null) fetchPropData();
        return propData;
    }
    public static ResultSet getHatData(){
        if(hatData == null) fetchHatData();
        return hatData;
    }
}