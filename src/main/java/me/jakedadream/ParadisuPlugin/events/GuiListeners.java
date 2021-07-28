package me.jakedadream.ParadisuPlugin.events;

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
        switch (player.getOpenInventory().getTitle()){
            case "§3§lProp Models Give GUI":
//                PropModelInv props = new PropModelInv();
                List<Inventory> invss = PropModelInv.getInvs();
                switch (e.getRawSlot()){
                    case 48:
                        e.setCancelled(true);
                        Inventory currinva = e.getInventory();


                        int indexa = invss.indexOf(currinva);
                        indexa -= 1;
                        if(indexa == -1) {
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexa));

                        break;
                    case 50:
                        e.setCancelled(true);
                        Inventory currinvb = e.getInventory();

                        int indexb = invss.indexOf(currinvb);
                        indexb += 1;
                        if (indexb == invss.size()){
                            e.setCancelled(true);
                            break;
                        }
                        player.closeInventory();
                        player.openInventory(invss.get(indexb));

                        break;
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case 32:
                    case 33:
                    case 34:
                    case 35:
                    case 36:
                    case 37:
                    case 38:
                    case 39:
                    case 40:
                    case 41:
                    case 42:
                    case 43:
                    case 44:
                        if(e.getCurrentItem() == null){
                            e.setCancelled(true);
                            return;
                        }
                        int modelData = e.getCurrentItem().getItemMeta().getCustomModelData();
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1){
                            player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have space in your inventory.");
                            e.setCancelled(true);
                            return;
                        }
                        player.getInventory().addItem(modelitemmanager.createPropModel(modelData));
                        e.setCancelled(true);
                        break;

                    default:
                        e.setCancelled(true);
                        break;
                }
        }
    }
}
