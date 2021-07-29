package me.jakedadream.ParadisuPlugin.events;

import me.jakedadream.ParadisuPlugin.modelmanager.HatModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.PropModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.modelitemmanager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class GuiListeners implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        HumanEntity player = e.getWhoClicked();
        if (!(player instanceof Player)) {
            player.sendMessage("no");
            return;
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
        }
    }
}
