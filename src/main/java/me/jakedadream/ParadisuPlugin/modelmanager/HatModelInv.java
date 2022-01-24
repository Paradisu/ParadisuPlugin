package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HatModelInv {
    public static List<Inventory> inventories = new ArrayList<>();

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();

    private static int getRows(ResultSet r){
        if(r == null) return 0;
        try{
            r.last();
            return r.getRow();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            try {
                r.beforeFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static void createInvs(){
        double dinvSize = 36;
        
        int rows = getRows(modelitemmanager.returnHatData());

        double neededInventories = (rows / dinvSize);
        int intneededInventories = (int) Math.ceil(neededInventories);

        for (int i = 0; i < intneededInventories; i ++){
            Inventory inv  = Bukkit.createInventory(null, 54, "§x§f§8§9§9§1§d§lHat Models Give GUI");
            inventories.add(inv);
        }
        int modelindex = 1;
        for (Inventory i : inventories){
            ItemStack previous = new ItemStack(Material.DIAMOND_AXE,1 );

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
            for (int a = modelindex; a < stop; a++){
                if (a > rows){
                    break;
                }
                ItemStack item = modelitemmanager.createHatModel(a);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(meta.getDisplayName() + " (" + a + ")");
                item.setItemMeta(meta);
                i.setItem(pageindex, item);
                pageindex++;
            }
            modelindex += 36;
        }
    }
    public static List<Inventory> getInvs() {return inventories;}
}
