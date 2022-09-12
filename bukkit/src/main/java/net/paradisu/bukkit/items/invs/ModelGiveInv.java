package net.paradisu.bukkit.items.invs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import net.paradisu.bukkit.items.common.menu.BlankItem;
import net.paradisu.bukkit.items.models.ModelInvManager;
import net.paradisu.bukkit.items.models.ModelItemManager;
import net.paradisu.bukkit.util.InventoryGUI;

public class ModelGiveInv implements InventoryGUI{

    private int page;
    private int lastPage;
    private boolean isProps;

    public ModelGiveInv(boolean isProps){
        page = 0;
        this.isProps = isProps;
        lastPage = ModelInvManager.getLastPage(isProps);
    }

    @Override
    public Inventory getInventory() {
        Inventory i = Bukkit.createInventory(this, 54, isProps ? "§x§f§8§9§9§1§d§lProp Models Give GUI" : "§x§f§8§9§9§1§d§lHat Models Give GUI");
        ItemStack[] models = ModelInvManager.getInv(isProps, page);
        
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

        for (int j = 0; j < 9; j++) i.setItem(j, new BlankItem());
        for(int j=9; j<45; j++)i.setItem(j, models[j-9]);
        for (int j=45; j<54; j++) i.setItem(j, new BlankItem());

        i.setItem(48, previous);
        i.setItem(50, next);

        return i;
    }
    

    @Override
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        switch (e.getRawSlot()) {
            case 48 -> {
                page--;
                if (page < 0) {
                    page =0;
                    return;
                }
                player.closeInventory();
                player.openInventory(this.getInventory());
            }
            case 50 -> {
                page++;
                if (page > lastPage){
                    page = lastPage;
                    return;
                } 
                player.closeInventory();
                player.openInventory(this.getInventory());
            }
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44 -> {
                if (e.getCurrentItem() == null) return;
                int modelData = e.getCurrentItem().getItemMeta().getCustomModelData();
                PlayerInventory inv = player.getInventory();
                int firstEmpty = inv.firstEmpty();
                if (firstEmpty == -1) {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have space in your inventory.");
                    return;
                }
                ItemStack item = isProps ? ModelItemManager.createPropModel(modelData) : ModelItemManager.createHatModel(modelData);
                player.getInventory().addItem(item);
            }
            default -> {return;}
        }
    }
    
}
