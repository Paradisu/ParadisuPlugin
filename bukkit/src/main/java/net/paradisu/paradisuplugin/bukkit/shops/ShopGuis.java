package net.paradisu.paradisuplugin.bukkit.shops;

import net.paradisu.paradisuplugin.bukkit.items.models.ModelItemManager;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGuis {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();


    public static List<Inventory> inventories = new ArrayList<>();

//    int arraylength;

    public static void initShops(){
        inventories.clear();
        FileConfiguration config = ParadisuMain.getFileShopGuiConfig();
//        modelitemmanager models = new modelitemmanager();
        Inventory blankinv = Bukkit.createInventory(null, 54, "placeholder");



        for (String s : config.getKeys(false)){
            ConfigurationSection cs = config.getConfigurationSection(s);
            Inventory inv = Bukkit.createInventory(null, 54, cs.getString("title"));
            int arrayLength = inventories.size();
            int neededLength = Integer.parseInt(s);
            inventories.add(blankinv);
            for (int a = arrayLength; a < neededLength; a++){
                inventories.add(blankinv);
            }
            inventories.set(Integer.parseInt(s), inv);
//            inventories.set(Integer.parseInt(s), inv);
            System.out.println(inventories);
        }

        for (Inventory i : inventories){
            if(inventories.indexOf(i) == 0){
                continue;
            }
            System.out.println(i);
            ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta blankMeta = blank.getItemMeta();
            blankMeta.setDisplayName(" ");
            blank.setItemMeta(blankMeta);

            String configsectionname = Integer.toString(inventories.indexOf(i));
            ConfigurationSection configsection = config.getConfigurationSection(configsectionname);

            for (int x = 0; x < 9; x++){
                i.setItem(x, blank);
            }
            for (int y = 45; y < 54; y++){
                i.setItem(y, blank);
            }
            List<String> items = new ArrayList<>();
            for (String slots : configsection.getKeys(false)) {
                items.add(slots);
            }
            items.remove("title");

            for (String slot : items){
                ItemStack shopItem;
                ConfigurationSection slotcs = configsection.getConfigurationSection(slot);
                if (slotcs.getBoolean("isProp")){
                    shopItem = new ItemStack(ModelItemManager.createPropModel(slotcs.getInt("modelid")));
                } else {
                    shopItem = new ItemStack(ModelItemManager.createHatModel(slotcs.getInt("modelid")));
                }
                ItemMeta shopItemMeta = shopItem.getItemMeta();
                List<String> itemlore = new ArrayList<>();
                itemlore.add("Price: " + String.valueOf(slotcs.getInt("price")));
                shopItemMeta.setLore(itemlore);
                shopItem.setItemMeta(shopItemMeta);
                i.setItem(Integer.parseInt(slot), shopItem);

            }
//            for (int z = 9; z < 44; z++){
//                ItemStack shopItem;
//                if(configsection.getConfigurationSection(String.valueOf(z)).getBoolean("isProp")){
//                    shopItem = new ItemStack(modelitemmanager.createPropModel(configsection.getInt(Integer.toString(z))));
//                } else {
//                   shopItem = new ItemStack((modelitemmanager.createHatModel(configsection.getInt(Integer.toString(z)))));
//                }
//
//            }
        }

    }

    public static List<Inventory> getShops() {
        return inventories;
    }
}
