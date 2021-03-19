package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import static org.bukkit.Bukkit.*;

public class snwevents implements Listener {

    @EventHandler
    public static void onJoin(PlayerJoinEvent jEvent) {
        Player player = jEvent.getPlayer();
        player.sendMessage("§f§l----------------------------");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fWelcome to §f§lSuper Nintendo World!");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fBe sure to do §e/rp §f& §e/audio§f!");
        player.sendMessage("§f§l----------------------------");
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent intEvent) {
        Player p = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {                    // Tests if you right click an armorstand
            if (intEvent.getRightClicked().getName().equals("CoinPickup")) {                     // Tests if the name is "CoinPickup"
                getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "A player picked up a coin");     // Triggers a message in chat
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1F, 1F);        // plays a sound to the player
                p.sendMessage("§3[§c§lS§b§lN§a§lW§3] §f§lYou just picked up a coin!");                          // Triggers another message in chat
                intEvent.getPlayer().getInventory().addItem(ItemManager.createCoin());                             // Adds an item into your inv (Can be used to trigger anything, like a gui)
                intEvent.getRightClicked().remove();
            }
        }
    }

    @EventHandler
    public static void onRightClick(PlayerInteractEvent rcevent) {
        Player user = rcevent.getPlayer();
        if (rcevent.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (rcevent.getItem() != null) {
                if (rcevent.getItem().getItemMeta().hashCode() == ItemManager.createThruWand().getItemMeta().hashCode()) {
                    if (user.hasPermission("worldedit.navigation.thru.command")) {
                        user.performCommand("thru");
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onRightClick2(PlayerInteractEvent rcevent2) {
        Player user = rcevent2.getPlayer();
        if (rcevent2.getAction() == Action.RIGHT_CLICK_AIR) {
            if (rcevent2.getItem() != null) {
                if (rcevent2.getItem().getItemMeta().hashCode() == ItemManager.createLLamazBanWand().getItemMeta().hashCode()) {
                    if (user.hasPermission("litebans.ban")) {
                        user.performCommand("litebans:ban Lord_of_llamaz §7§lLlamaz ban wand ban: You were doing something to piss someone off. Stop it.");
                    }
                }
            }
        }
    }

    @EventHandler
    public static void WearHatEvent(PlayerInteractEvent wearhat) {
        Player player = wearhat.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR)
            return;
        switch (((Damageable) player.getEquipment().getItemInMainHand().getItemMeta()).getDamage()) {
            case 16:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 41:
            case 53:
            case 60:
            case 64:
            case 65:
            case 66:
            case 76:
            case 77:
            case 78:
                if (wearhat.getAction() == Action.RIGHT_CLICK_AIR) {

                    if (wearhat.getItem() != null) {
                        ItemStack[] armor = player.getInventory().getArmorContents();
                        ItemStack swap = armor[3];
                        armor[3] = player.getEquipment().getItemInMainHand();
                        player.getInventory().setArmorContents(armor);
                        player.getInventory().setItemInMainHand(swap);


                        break;

                    }
                }
        }
    }

    @EventHandler
    public static void TableFlipEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":tableflip:", "(╯°□°）╯︵ ┻━┻");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void DabEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":shrug:", "¯⧹_(ツ)_/¯");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void GunEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":gun:", "▄︻̷̿┻̿═━一");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void HappyEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":happy:", "(^⌣^)");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void WaveEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":wave:", "(^⌣^)ノ");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void UnflipEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":unflip:", "┬─┬ノ( º _ ºノ)");
        pchat.setMessage(msg);
    }
    @EventHandler()
    public void onRClick(PlayerInteractAtEntityEvent intEvent) {
        Player player = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if (intEvent.getRightClicked().getName().equals("TRASHCAN")) {
                trashcans inv = new trashcans();

                inv.TrashCanInv(player);
            }
        }
    }
    @EventHandler
    public void InvenClick(InventoryClickEvent event) {
        Player player = event.getWhoClicked();

        Inventory open = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();
        ItemManager createAcceptButton = new ItemManager();
        ItemManager createBlankButton = new ItemManager();
        ItemManager createDenyButton = new ItemManager();

        if(open == null){
            return;
        }

        if(open.equals()) {         // <!> PLEASE REMEMBER OT DO THIS IDK HOW TO PLZ RMEMBER I NEED TO LIKE SET THE INVENTORY TO BE THE RIGHT ONE OR SOMETHINNNN WOOT
            event.setCancelled(true);

            if (item.equals(null) || item.hasItemMeta()) {
                return;
            }

            if (item.getItemMeta().equals(createAcceptButton)) {            // Please remember to like make it so when you click this happens
                trashcans ntc = new trashcans();
                event.isCancelled(true);
                event.getClickedInventory().setItem(1, Material.AIR);// CLEAR ALL ITEMS 1-27
                ntc.TrashCanInv(player);

            }

            if (item.getItemMeta().equals(createDenyButton)) {            // Please remember to like make it so when you click this happens
                event.isCancelled(true);
                // GIVES ALL THE ITEMS BACK AND CLOSES TRASHCAN
            }
            if (item.getItemMeta().equals(createBlankButton)) {            // Please remember to like make it so when you click this happens
                event.isCancelled(true);
                // Do nothing lolololololol
            }
        }
    }
}