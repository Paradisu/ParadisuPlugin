package net.paradisu.ParadisuPlugin.events;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import net.paradisu.ParadisuPlugin.ParadisuMain;
import net.paradisu.ParadisuPlugin.items.models.ModelGiveInv;
import net.paradisu.ParadisuPlugin.items.models.ModelItemManager;
import net.paradisu.ParadisuPlugin.shops.PlayerInventories;
import net.paradisu.ParadisuPlugin.shops.ShopGuis;

public class GuiListeners implements Listener {


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();


    @EventHandler
    public void onClick(InventoryClickEvent e){

        HumanEntity player = e.getWhoClicked();
        if (!(player instanceof Player)) {
            player.sendMessage("no");
            return;
        }
        if (player.getOpenInventory().getTitle().toLowerCase().contains("shop")){
            switch (e.getRawSlot()){
                case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 ->{
                    e.setCancelled(true);
                    PlayerInventories pInvs = new PlayerInventories();

                    if (e.getInventory().getItem(e.getRawSlot()) == null) {
                        return;
                    }
                    PlayerInventory inv = player.getInventory();
                    int firstEmpty = inv.firstEmpty();
                    if (firstEmpty == -1) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §7Your inventory is full");
                        return;
                    }
                    ItemStack playerItem;
                    int arrayIndex = ShopGuis.getShops().indexOf(e.getInventory());
                    // COME BACK TO HERE
                    ConfigurationSection configSection = ParadisuMain.getFileShopGuiConfig().getConfigurationSection(Integer.toString(arrayIndex)).getConfigurationSection(Integer.toString(e.getRawSlot()));

                    if (pInvs.getGoldNuggets((Player) player) >= configSection.getInt("price")){
//                        player.sendMessage("you have enough money");
                        pInvs.spendGoldNuggets((Player) player, configSection.getInt("price"));
//                        player.sendMessage("youspent ur money");
                        if (e.getInventory().getItem(e.getRawSlot()).getType() == Material.DIAMOND_AXE){
                            playerItem = ModelItemManager.createPropModel(e.getInventory().getItem(e.getRawSlot()).getItemMeta().getCustomModelData());
                        } else {
                            playerItem = ModelItemManager.createHatModel(e.getInventory().getItem(e.getRawSlot()).getItemMeta().getCustomModelData());
                        }
                        player.getInventory().addItem(playerItem);
                        player.sendMessage("§3[§dParadisu §bツ§3] §fCompleted purchase.");

                    } else {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fYou do not have enough money to complete that purchase.");
                    }

                }
                default -> e.setCancelled(true);

            }
        }
        switch (player.getOpenInventory().getTitle()) {
            case "§x§f§8§9§9§1§d§lProp Models Give GUI" -> {
//                PropModelInv props = new PropModelInv();
                List<Inventory> invss = ModelGiveInv.getInvs(true);
                switch (e.getRawSlot()) {
                    case 48 -> {
                        e.setCancelled(true);
                        Inventory currinva = e.getInventory();
                        int indexa = invss.indexOf(currinva);
                        indexa -= 1;
                        if (indexa == -1) {
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexa));
                    }
                    case 50 -> {
                        e.setCancelled(true);
                        Inventory currinvb = e.getInventory();
                        int indexb = invss.indexOf(currinvb);
                        indexb += 1;
                        if (indexb == invss.size()) {
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexb));
                    }
                    case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                        if (e.getCurrentItem() == null) {
                            e.setCancelled(true);
                            return;
                        }
                        int modelData = e.getCurrentItem().getItemMeta().getCustomModelData();
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have space in your inventory.");
                            e.setCancelled(true);
                            return;
                        }
                        ItemStack item = ModelItemManager.createPropModel(modelData);
                        player.sendMessage("display name: " + item.getItemMeta().getDisplayName());
                        player.getInventory().addItem(ModelItemManager.createPropModel(modelData));
                        e.setCancelled(true);
                    }
                    default -> e.setCancelled(true);
                }
            }
            case "§x§f§8§9§9§1§d§lHat Models Give GUI" -> {
                List<Inventory> invss = ModelGiveInv.getInvs(false);
                switch (e.getRawSlot()) {
                    case 48 -> {
                        e.setCancelled(true);
                        Inventory currinva = e.getInventory();
                        int indexa = invss.indexOf(currinva);
                        indexa -= 1;
                        if (indexa == -1){
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexa));
                    }
                    case 50 -> {
                        e.setCancelled(true);
                        Inventory currinvb = e.getInventory();
                        int indexb = invss.indexOf(currinvb);
                        indexb += 1;
                        if (indexb == invss.size()) {
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexb));
                    }
                    case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                        if (e.getCurrentItem() == null) {
                            e.setCancelled(true);
                            return;
                        }
                        int modelData = e.getCurrentItem().getItemMeta().getCustomModelData();
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have space in your inventory.");
                            e.setCancelled(true);
                            return;
                        }
                        player.getInventory().addItem(ModelItemManager.createHatModel(modelData));
                        e.setCancelled(true);
                    }
                    default -> e.setCancelled(true);
                }
            }

            case "§3✮ §dTRASHCAN §3✮"  -> {
                switch (e.getRawSlot()) {
                    case 27, 28, 29, 30, 31, 32, 33, 34 -> {
                        if (e.getCurrentItem() == null) {
                            e.setCancelled(true);
                            return;
                        }
                    }

                    case 35 -> {
                        e.setCancelled(true);
                        for (ItemStack item: e.getInventory().getContents()) {
                            for (ItemStack items : e.getInventory().getContents()) {
                                if (items != null) {
                                    if (e.getRawSlot() < 27) {
                                        Material slotsmat = e.getCurrentItem().getType();
                                        Integer numbermat = e.getCurrentItem().getAmount();
                                        ItemMeta metamat = e.getCurrentItem().getItemMeta();

                                        item.setAmount(numbermat);
                                        item.setType(slotsmat);
                                        item.setItemMeta(metamat);



                                        player.getInventory().addItem(items);
                                    }
                                }
                            }
                        }
                        return;
                    }

                    default -> e.setCancelled(false);
                }
            }
        }
        if (player.getOpenInventory().getTitle().contains("'s §3Inventory")) {
            switch (e.getRawSlot()) {
                    case 36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 48, 49:
                        e.setCancelled(true);
                        break;

                    case 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,31,32,33,34,35:
                        if (player.hasPermission("snw.invsee")) {
                            e.setCancelled(true);
                        } else if (player.hasPermission("snw.invsee.take")) {
                            break;
                        }

                    case 45,47,50,51,52,53:
                        if (player.hasPermission("snw.invsee")) {
                            e.setCancelled(true);
                        } else if (player.hasPermission("snw.invsee.take")) {
                            break;
                        }

                default:
                    return;
            }
        }
    }
}
