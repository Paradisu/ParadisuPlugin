package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.items.ItemManager;
import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PropModelInv {
    public static List<Inventory> inventories = new ArrayList<>();


    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();


    public static void createInvs(){

        modelitemmanager models = new modelitemmanager();
        double dinvSize = 36;
        int invSize = 36;

        double neededInventories = (paradisumain.getPropModelsConfig().getKeys(false).size() / dinvSize);
        int intneededInventories = (int) Math.ceil(neededInventories);

        System.out.println("keys: " + paradisumain.getPropModelsConfig().getKeys(false).size());
        System.out.println("div: " + paradisumain.getPropModelsConfig().getKeys(false).size() / dinvSize);
        System.out.println("invs: " + neededInventories);
        System.out.println("intinvs " + intneededInventories);
        for (int i = 0; i < intneededInventories; i++){
            Inventory inv = Bukkit.createInventory(null, 54, "§3§lProp Models Give GUI");
            inventories.add(inv);
        }
        int modelindex = 1;
        for (Inventory i : inventories){
            ItemStack previous = new ItemStack(Material.DIAMOND_AXE, 1);

            ItemMeta prevMeta = previous.getItemMeta();
            assert prevMeta != null;
            prevMeta.setCustomModelData(28);
            prevMeta.setDisplayName("§b§l←§d§l Go Left");
            List<String> prevLore = new ArrayList<>();
            prevLore.add("§7Go left a page.");
            prevMeta.setLore(prevLore);
            previous.setItemMeta(prevMeta);

            ItemStack next = new ItemStack(Material.DIAMOND_AXE, 1);
            ItemMeta nextMeta = next.getItemMeta();
            assert nextMeta != null;
            nextMeta.setCustomModelData(48);

            nextMeta.setDisplayName("§d§lGo Right §b§l→");
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
                if (a > paradisumain.getPropModelsConfig().getKeys(false).size()){
                    break;
                }
                ItemStack item = modelitemmanager.createPropModel(a);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(meta.getDisplayName() + " (" + a + ")");
                item.setItemMeta(meta);
                i.setItem(pageindex, item);
                pageindex++;

            }
            modelindex += 36;
        }

    }
    public static List<Inventory> getInvs(){
        return inventories;
    }


//    List<Inventory> inventories = new ArrayList<>();


}
