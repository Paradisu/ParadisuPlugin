package me.jakedadream.ParadisuPlugin.modelmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.jakedadream.ParadisuPlugin.ParadisuMain;

public abstract class ModelInv {
    
    public static List<Inventory> propInventories = new ArrayList<Inventory>();
    public static List<Inventory> hatInventories = new ArrayList<Inventory>();

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    public static void createInvs(boolean isProps){

        List<Inventory> inventories = isProps ? propInventories : hatInventories;

        double dinvSize = 36;
        
        int items = isProps ? modelitemmanager.getPropCount() : modelitemmanager.getHatCount();

        
        double neededInventories = (items) / dinvSize;
        int intneededInventories = (int) Math.ceil(neededInventories);

        // System.out.println("keys: " + rows);
        // System.out.println("div: " + rows / dinvSize);
        // System.out.println("invs: " + neededInventories);
        // System.out.println("intinvs " + intneededInventories);
        for (int i = 0; i < intneededInventories; i++){
            String s = isProps ? "§x§f§8§9§9§1§d§lProp Models Give GUI" : "§x§f§8§9§9§1§d§lHat Models Give GUI";
            Inventory inv = Bukkit.createInventory(null, 54, s);
            inventories.add(inv);
        }
        int modelindex = 1;

        // Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "num invs: " + inventories.size());
        for (Inventory i : inventories){
            
            
            ItemStack previous = new ItemStack(Material.DIAMOND_AXE, 1);

            ItemMeta prevMeta = previous.getItemMeta();
            assert prevMeta != null;
            prevMeta.setCustomModelData(28);
            prevMeta.setDisplayName("§x§e§8§1§e§2§6§l←§x§f§8§9§9§1§d§l Go Left");
            List<String> prevLore = new ArrayList<>();
            prevLore.add("§7Go left a page.");
            prevMeta.setLore(prevLore);
            previous.setItemMeta(prevMeta);

            ItemStack next = new ItemStack(Material.DIAMOND_AXE, 1);
            ItemMeta nextMeta = next.getItemMeta();
            assert nextMeta != null;
            nextMeta.setCustomModelData(48);

            nextMeta.setDisplayName("§x§e§8§1§e§2§6§lGo Right §x§f§8§9§9§1§d§l→");
            List<String> nextLore = new ArrayList<>();
            nextLore.add("§7Go right a page.");
            nextMeta.setLore(nextLore);
            next.setItemMeta(nextMeta);

            i.setItem(48, previous);
            i.setItem(50, next);

            ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blankMeta = blank.getItemMeta();
            blankMeta.setDisplayName(" ");
            blank.setItemMeta(blankMeta);

            for (int j = 0; j < 9; j++){
                i.setItem(j, blank);
            }

            i.setItem(45, blank);
            i.setItem(46, blank);
            i.setItem(47, blank);
            i.setItem(49, blank);
            i.setItem(51, blank);
            i.setItem(52, blank);
            i.setItem(53, blank);

            int pageindex = 9;
            int stop = modelindex + 36;
            for (int a = modelindex; a < stop; a++) {
                if (a > items){
                    break;
                }
                // ItemStack item = modelitemmanager.createPropModel(a);
                ItemStack item = isProps ? modelitemmanager.createPropModel(a) : modelitemmanager.createHatModel(a);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(meta.getDisplayName() + ChatColor.GOLD + " (" + a + ")");
                item.setItemMeta(meta);
                i.setItem(pageindex, item);
                pageindex++;

            }
            modelindex += 36;
        }

    }
    public static List<Inventory> getInvs(boolean isProps){
        return isProps ? propInventories : hatInventories;
    }
    public static void createAllInvs(){
        // Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "creating all invs");
        createInvs(true);
        createInvs(false);
    }

    public static Inventory getFirstInv(boolean isProps){
        return isProps ? propInventories.get(0) : hatInventories.get(0);
    }
}
