package me.jakedadream.ParadisuPlugin.events;

import me.jakedadream.ParadisuPlugin.items.PlayerInventories;
import me.jakedadream.ParadisuPlugin.modelmanager.HatModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.PropModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.modelitemmanager;
import me.jakedadream.ParadisuPlugin.paradisumain;
import me.jakedadream.ParadisuPlugin.shops.ShopGuis;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuiListeners implements Listener {


    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();


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
                    ConfigurationSection configSection = paradisumain.getFileShopGuiConfig().getConfigurationSection(Integer.toString(arrayIndex)).getConfigurationSection(Integer.toString(e.getRawSlot()));

                    if (pInvs.getGoldNuggets((Player) player) >= configSection.getInt("price")){
//                        player.sendMessage("you have enough money");
                        pInvs.spendGoldNuggets((Player) player, configSection.getInt("price"));
//                        player.sendMessage("youspent ur money");
                        if (e.getInventory().getItem(e.getRawSlot()).getType() == Material.DIAMOND_AXE){
                            playerItem = modelitemmanager.createPropModel(e.getInventory().getItem(e.getRawSlot()).getItemMeta().getCustomModelData());
                        } else {
                            playerItem = modelitemmanager.createHatModel(e.getInventory().getItem(e.getRawSlot()).getItemMeta().getCustomModelData());
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
            case "§3§lProp Models Give GUI" -> {
//                PropModelInv props = new PropModelInv();
                List<Inventory> invss = PropModelInv.getInvs();
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
                        player.getInventory().addItem(modelitemmanager.createPropModel(modelData));
                        e.setCancelled(true);
                    }
                    default -> e.setCancelled(true);
                }
            }
            case "§3§lHat Models Give GUI" -> {
                List<Inventory> invss = HatModelInv.getInvs();
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
                        player.getInventory().addItem(modelitemmanager.createHatModel(modelData));
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
