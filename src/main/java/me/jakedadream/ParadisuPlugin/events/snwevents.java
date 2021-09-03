package me.jakedadream.ParadisuPlugin.events;

import me.jakedadream.ParadisuPlugin.items.ItemManager;
import me.jakedadream.ParadisuPlugin.invs.trashcanINV;
import me.jakedadream.ParadisuPlugin.items.PluginInventories;
import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

import static org.bukkit.Bukkit.*;

public class snwevents implements Listener {

    private static HashMap<Player, ItemStack> ident = new HashMap<>();

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();



    @EventHandler
    public static void onJoin(PlayerJoinEvent jEvent) {
        Player joiner = jEvent.getPlayer();
        joiner.sendMessage("§f§l----------------------------");
        joiner.sendMessage("§3[§dParadisu §bツ§3] §fWelcome to §f§lSuper Nintendo World!");
        joiner.sendMessage("§3[§dParadisu §bツ§3] §fBe sure to do §e/rp §f& §e/audio§f!");
        joiner.sendMessage("§f§l----------------------------");

        if (!joiner.hasPermission("snw.nospawnonjoin")) {
            Location loc = new Location((Bukkit.getWorld("SuperNW")), 82.5, 86.1, -741.5, 75, 0);    // 82.5 86.1 -741.5
            joiner.setGameMode(GameMode.ADVENTURE);
            joiner.teleport(loc);
        }

    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent intEvent) {
        Player p = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {                    // Tests if you right click an armorstand
            if (intEvent.getRightClicked().getName().equals("CoinPickup")) {                     // Tests if the name is "CoinPickup"
                getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "A player picked up a coin");     // Triggers a message in chat
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1F, 1F);        // plays a sound to the player
                p.sendMessage("§3[§dParadisu §bツ§3] §f§lYou just picked up a coin!");                          // Triggers another message in chat
                intEvent.getPlayer().getInventory().addItem(ItemManager.createCoin());                             // Adds an item into your inv (Can be used to trigger anything, like a gui)
                intEvent.getRightClicked().remove();
            }
        }
    }


    @EventHandler
    public static void WearHatEvent(PlayerInteractEvent wearhat) {
        Player player = wearhat.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (wearhat.getAction() == Action.RIGHT_CLICK_AIR) {
            if (wearhat.getItem() != null && wearhat.getItem().getType() == Material.CARVED_PUMPKIN) {
                ItemStack[] armor = player.getInventory().getArmorContents();
                ItemStack swap = armor[3];
                armor[3] = player.getEquipment().getItemInMainHand();
                player.getInventory().setArmorContents(armor);
                player.getInventory().setItemInMainHand(swap);
            }
        }
    }

    @EventHandler()
    public void onRClick(PlayerInteractAtEntityEvent intEvent) {
        Player player = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if (intEvent.getRightClicked().getName().equals("TRASHCAN")) {

                player.openInventory(trashcanINV.TrashcanGUI());
            }
        }
    }

    @EventHandler
    public void InvenClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Inventory open = event.getClickedInventory();
        ItemStack item = event.getCurrentItem();

        if (open == null) {
            return;
        }

        if (player.getOpenInventory().getTitle().equals("§3§lWaste Bin")) {
            ident.put(player, event.getCurrentItem());

            if (item == null || event.getSlot() > 26) {
                return;
            }

            }
        }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (paradisumain.getPlugin(paradisumain.class).getConfig().getString(e.getPlayer().getName()) != null) {
            e.getPlayer().setDisplayName(paradisumain.getPlugin(paradisumain.class).getConfig().getString(e.getPlayer().getName()));
        }
    }    //////////////////// FIX THIS

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
            e.setJoinMessage("§2[§a§l+§2]§f " + joiner.getName() + " joined for the first time; Make sure to answer their questions!");
        } else {
            e.setJoinMessage("§2[§a§l+§2]§f " + joiner.getName() + " joined!");
        }
    }

    @EventHandler
    public void PlayerCraft(CraftItemEvent cie) {
        cie.setCancelled(true);
    }

    @EventHandler
    public void PlayerHungry(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        player.setSaturation(20);
    }

    /*
    @EventHandler
    public void PlayerPunchBlock(PlayerInteractEvent pie) {
        if (pie.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = pie.getPlayer();
            Block b = pie.getClickedBlock();
            if (b.getType() != Material.CAULDRON)
                return;  if (b.getType() == Material.CAULDRON) {
                int full = ((Levelled)b.getBlockData()).getLevel();
                if (((Levelled)b.getBlockData()).getLevel() == 3) {
                    ConfigurationSection pd = getConfig().getConfigurationSection("playerdata").getConfigurationSection(player.getUniqueId().toString() + ", " + player.getName());
                    if (pd.getInt("lb") <= 4) {
                        pd.set("lb", Integer.valueOf(pd.getInt("lb") + 1));
                        saveConfig();
                        player.sendMessage(getConfig().getString("message-prefix") + " §rYou have claimed §e§L(" + pd.getInt("lb") + "/5)§r lucky blocks");
                        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);
                    } else {
                        player.sendMessage(getConfig().getString("message-prefix") + " §rYou have claimed the maximum lucky blocks for the day.");
                        player.playSound(player.getLocation(), Sound.ENTITY_DROWNED_HURT, 1.0F, 1.0F);
                    }
                }
            }
        }
    }
} */


    @EventHandler
    public void EatFoodEvent(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        ItemStack items = e.getItem();
        String itemconsumed = items.getItemMeta().getLocalizedName();
        if (e.getItem().getType() == Material.HONEY_BOTTLE) {

            Integer seconds = 600; // 10 minutes in seconds

            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20, 1, true, false));
            p.sendMessage("§3[§dParadisu §bツ§3] §fYou consumed a drink/food item and were given speed for§3 " + seconds/60 + "§f minutes.");
        }
    }







}