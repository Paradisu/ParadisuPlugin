package me.jakedadream.snwplugin.events;

import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.client.dispatch.PacketDispatch;
import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
    public void onPlayerInteractAtEntity (PlayerInteractAtEntityEvent intEvent) {
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
            if (rcevent.getItem() !=null) {
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
                        user.performCommand("litebans:ban Lord_of_llamaz §b§lGet Rekt Nerd");
                    }
                }
            }
        }
    }
}