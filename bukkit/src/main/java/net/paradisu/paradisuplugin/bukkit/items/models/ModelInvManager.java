package net.paradisu.paradisuplugin.bukkit.items.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public abstract class ModelInvManager {
    
    public static List<ItemStack[]> propInventories = new ArrayList<ItemStack[]>();
    public static List<ItemStack[]> hatInventories = new ArrayList<ItemStack[]>();

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    public static void createInvs(boolean isProps){

        List<ItemStack[]> inventories = isProps ? propInventories : hatInventories;

        int invSize = 36;
        int numItems = isProps ? ModelItemManager.getPropCount() : ModelItemManager.getHatCount();
        int neededInventories = (numItems-1)/invSize + 1;
        
        for (int i = 0; i < neededInventories; i++){
            ItemStack[] items = new ItemStack[invSize];
            for(int j = 0; j < invSize; j++){
                if(i*invSize+(j+1) > numItems) break;
                ItemStack item = isProps ? ModelItemManager.createPropModel(i*invSize+(j+1)) : ModelItemManager.createHatModel(i*invSize+(j+1));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(meta.getDisplayName() + " §7(" + (i*invSize+(j+1)) + ")");
                item.setItemMeta(meta);
                items[j] = item;
            }
            inventories.add(items);
        }
    }
    public static List<ItemStack[]> getInvs(boolean isProps){
        return isProps ? propInventories : hatInventories;
    }
    public static void createAllInvs(){
        // Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "creating all invs");
        createInvs(true);
        createInvs(false);
    }

    public static ItemStack[] getFirstInv(boolean isProps){
        return isProps ? propInventories.get(0) : hatInventories.get(0);
    }

    public static ItemStack[] getInv(boolean isProps, int index){
        return isProps ? propInventories.get(index) : hatInventories.get(index);
    }

    public static int getLastPage(boolean isProps){
        return isProps ? propInventories.size()-1 : hatInventories.size()-1;
    }
}
