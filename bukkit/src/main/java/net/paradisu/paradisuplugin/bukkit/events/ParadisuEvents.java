package net.paradisu.paradisuplugin.bukkit.events;

import static org.bukkit.Bukkit.getServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import net.paradisu.paradisuplugin.bukkit.items.GenItemManager;
import net.paradisu.paradisuplugin.bukkit.items.invs.TrashCan;
import net.paradisu.paradisuplugin.bukkit.util.InventoryGUI;

public class ParadisuEvents implements Listener {

    // private static HashMap<Player, ItemStack> ident = new HashMap<>();

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    private DataSource dataSource;

    public ParadisuEvents(){
        dataSource = ParadisuMain.getDBCon();
    }
    
   
    

    @EventHandler
    public void onJoin(PlayerJoinEvent jEvent) {
        Player joiner = jEvent.getPlayer();
        joiner.sendMessage("\uE013 §f| \uE013" + "\n" +
        "\uE016 §fWelcome to " + cmdemph + "\ue00f" + "\n" +
        "\uE016 §fBe sure to do " + cmdemph + "/audio§f." + "\n" +
        "\uE013 §f| \uE013"
        );

        if (!joiner.hasPermission("paradisu.nospawnonjoin")) {
            World world = joiner.getWorld();
            Location loc = new Location(world, 82.5, 86.1, -741.5, 75, 0);    // 82.5 86.1 -741.5
            loc.setWorld(world); // this is just to remove the warning
            joiner.setGameMode(GameMode.ADVENTURE);
            //joiner.teleport(loc);
        } 
    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent intEvent) {
        Player p = intEvent.getPlayer();
        String playeruuid = p.getUniqueId().toString();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {                    // Tests if you right click an armorstand
            if (intEvent.getRightClicked().getName().equals("CoinPickup")) {                     // Tests if the name is "CoinPickup"
                getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "A player picked up a coin");     // Triggers a message in chat
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1F, 1F);        // plays a sound to the player
                p.sendMessage(cmdprefix + "§fYou just picked up a coin!");                          // Triggers another message in chat
                intEvent.getPlayer().getInventory().addItem(GenItemManager.createCoin());                             // Adds an item into your inv (Can be used to trigger anything, like a gui)
                intEvent.getRightClicked().remove();
                
                try (Connection connection = dataSource.getConnection(); 
                     PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET picked_up_coin_amount = picked_up_coin_amount + 1 WHERE uuid = ?")) {
                    
                    statement.setString(1, playeruuid);
                    statement.executeQuery();
                    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Changed a player's amount of coins");

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @EventHandler
    public static void WearHatEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack handItem = event.getItem();
        ItemStack headItem = player.getInventory().getHelmet();
        int slot = player.getInventory().getHeldItemSlot();
        
        if (headItem == null) { headItem = new ItemStack(Material.AIR); } else { headItem = headItem.clone();}
        if (handItem == null) { handItem = new ItemStack(Material.AIR); } else { handItem = handItem.clone();}

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (handItem.getType() == Material.CARVED_PUMPKIN) {
                playerInventory.setHelmet(handItem);
                playerInventory.setItem(slot, headItem);
                player.playSound(player, Sound.BLOCK_LAVA_POP, SoundCategory.MASTER, 1F, 1F);
            }
        }   
    }

    @EventHandler
    public void onRClick(PlayerInteractAtEntityEvent intEvent) {
        Player player = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if (intEvent.getRightClicked().getName().equals("TRASHCAN")) {

                player.openInventory(new TrashCan().getInventory());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // e.setCancelled(true);
        if(e.getInventory().getHolder() instanceof InventoryGUI) {
            e.setCancelled(true);
            InventoryGUI gui = (InventoryGUI) e.getInventory().getHolder();
            gui.onClick(e);
        }      
    }
    

    @EventHandler
    public void leaveEvent(PlayerQuitEvent e) {
        Player leaver = e.getPlayer();

        e.setQuitMessage("§4[§c§l-§4]§f " + leaver.getName() + " left!");
    }


    @EventHandler
    public void onjoin(PlayerJoinEvent e) {
        Player joiner = e.getPlayer();
        boolean hasjoined = joiner.hasPlayedBefore();

        if (!hasjoined) {
            e.setJoinMessage("§2[§a§l+§2]§f " + joiner.getName() + " joined for the first time!");
        } else {
            e.setJoinMessage("§2[§a§l+§2]§f " + joiner.getName() + " joined!");
        }
    }

    @EventHandler
    public void PlayerCraft(CraftItemEvent cie) {
        if (!cie.getWhoClicked().hasPermission("paradisu.allowcraft")) {
            cie.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerHungry(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        player.setSaturation(20);
    }



    @EventHandler
    public void EatFoodEvent(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        // ItemStack items = e.getItem();
        // String itemconsumed = items.getItemMeta().getLocalizedName();
        if (e.getItem().getType() == Material.HONEY_BOTTLE) {

            Integer seconds = 600; // 10 minutes in seconds

            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20, 1, true, false));
            p.sendMessage(cmdprefix + "§fYou consumed an item and were given speed for " + cmdemph + seconds/60 + "§f minutes.");
        }
    }
}
