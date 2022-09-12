package net.paradisu.bukkit.events;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.paradisu.bukkit.ParadisuMain;
import net.paradisu.bukkit.items.models.ModelItemManager;
import net.paradisu.bukkit.shops.PlayerInventories;
import net.paradisu.bukkit.shops.ShopGuis;

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
        
        if (player.getOpenInventory().getTitle().contains("'s §3Inventory")) {
            switch (e.getRawSlot()) {
                    case 36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 48, 49:
                        e.setCancelled(true);
                        break;

                    case 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,31,32,33,34,35:
                        if (player.hasPermission("paradisu.invsee")) {
                            e.setCancelled(true);
                        } else if (player.hasPermission("paradisu.invsee.take")) {
                            break;
                        }

                    case 45,47,50,51,52,53:
                        if (player.hasPermission("paradisu.invsee")) {
                            e.setCancelled(true);
                        } else if (player.hasPermission("paradisu.invsee.take")) {
                            break;
                        }

                default:
                    return;
            }
        }
    }
}
