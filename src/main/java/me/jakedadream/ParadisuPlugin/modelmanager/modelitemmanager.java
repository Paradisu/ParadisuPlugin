package me.jakedadream.ParadisuPlugin.modelmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jakedadream.ParadisuPlugin.ParadisuMain;

public class modelitemmanager {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    private static ArrayList<ItemStack> props = new ArrayList<ItemStack>();
    private static ArrayList<ItemStack> hats = new ArrayList<ItemStack>();


    public static void updateModelData(){
        props.clear();
        hats.clear();

        try (Connection c = ParadisuMain.getDBCon().getConnection())  {    
            try(PreparedStatement query = c.prepareStatement("SELECT * FROM PropModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = query.executeQuery()){
                readData(rs, props, true);
            } catch (SQLException e){
                e.printStackTrace();
            }

            try(PreparedStatement query = c.prepareStatement("SELECT * FROM HatModels", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = query.executeQuery()){
                readData(rs, hats, false);
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    

    private static void readData(ResultSet rs, ArrayList<ItemStack> list, boolean isProps) {
        try{
            rs.beforeFirst();
            while(rs.next()){
                Material material = isProps ? Material.DIAMOND_AXE : Material.CARVED_PUMPKIN;

                ItemStack i = new ItemStack(material, 1);
                ItemMeta meta = i.getItemMeta();

                meta.setCustomModelData(rs.getInt("customModelData"));
                meta.setDisplayName(rs.getString("displayname"));

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                i.setItemMeta(meta);

                

                list.add(i);
                //Bukkit.getConsoleSender().sendMessage("adding " + props.get(i.getItemMeta().getCustomModelData()-1).getItemMeta().getDisplayName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack createHatModel(int index){
        if(hats.size()==0) updateModelData();

        return hats.get(index-1).clone();
    }

    public static ItemStack createPropModel(int index){
        
        if(props.size()==0) 
            updateModelData();
        
        return props.get(index-1).clone();
    }

    public static int getHatCount(){
        return hats.size();
    }
    public static int getPropCount(){
        return props.size();
    }
}