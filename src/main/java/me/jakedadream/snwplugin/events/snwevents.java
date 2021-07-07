package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.items.ItemManager;
import me.jakedadream.snwplugin.items.PluginInventories;
import me.jakedadream.snwplugin.snwplugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

import static org.bukkit.Bukkit.*;

public class snwevents implements Listener {

    private static HashMap<Player, ItemStack> ident = new HashMap<>();

    @EventHandler
    public static void onJoin(PlayerJoinEvent jEvent) {
        Player joiner = jEvent.getPlayer();
        joiner.sendMessage("§f§l----------------------------");
        joiner.sendMessage("§3[§dParadisu §bツ§3] §fWelcome to §f§lSuper Nintendo World!");
        joiner.sendMessage("§3[§dParadisu §bツ§3] §fBe sure to do §e/rp §f& §e/audio§f!");
        joiner.sendMessage("§f§l----------------------------");

        if (!joiner.hasPermission("snw.nospawnonjoin")) {
            Location loc = new Location((Bukkit.getWorld("SuperNW")), 82.5, 86.1, -741.5, 75, 0);    // 82.5 86.1 -741.5
            joiner.teleport(loc);
        }

        joiner.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 1, false));
        joiner.setGameMode(GameMode.ADVENTURE);

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
    public static void HappyEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":happy:", "(^⌣^)");
        pchat.setMessage(msg);
    }

    @EventHandler
    public static void BangEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":interrobang:", "§4§l!?");
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

    @EventHandler
    public static void welcomeevent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        msg = msg.replace(":Welcome!", "┬─┬ノ( º _ ºノ)");
        pchat.setMessage(msg);
    }



    @EventHandler()
    public void onRClick(PlayerInteractAtEntityEvent intEvent) {
        Player player = intEvent.getPlayer();
        if (intEvent.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            if (intEvent.getRightClicked().getName().equals("TRASHCAN")) {
                PluginInventories inv = new PluginInventories();

                inv.TrashCanInv(player);
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

            if (item.getItemMeta().equals(ItemManager.acceptbutton.getItemMeta())) {
                player.getOpenInventory().getTopInventory().clear();
                // Delete the items and reopen a new Trashcan inv using 'trashcans.TrashCanInv(player);'
            }

            if (item.getItemMeta().equals(ItemManager.denybutton.getItemMeta())) {
                for (int i = 0; i < 27; i++) {
                    player.getInventory().addItem(open.getItem(i));
                }
                event.setCancelled(true);
                // GIVES ALL THE ITEMS BACK AND CLOSES TRASHCAN
            }

            if (item.getItemMeta().equals(ItemManager.blankbutton.getItemMeta())) {
                event.setCancelled(true);
            }
        }
    }
/*
    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.getOpenInventory().getTitle().equals("§3§lWaste Bin")) {
            ItemStack[] invcontents = event.getInventory().getContents();
            PluginInventories inv = new PluginInventories();
            inv.TrashCanInv(player);
            player.getOpenInventory().getTopInventory().setContents(invcontents);

            // Ramen help im in pain

        } else {
            return;
        }
    }    //////////////////// FIX THIS

*/
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (snwplugin.getPlugin(snwplugin.class).getConfig().getString(e.getPlayer().getName()) != null) {
            e.getPlayer().setDisplayName(snwplugin.getPlugin(snwplugin.class).getConfig().getString(e.getPlayer().getName()));
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
        cie.setCancelled(false);
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
    public void playerResourcePack(PlayerResourcePackStatusEvent rp) {
        Player player = rp.getPlayer();
        rp.getStatus();
        if ((rp.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED || rp.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
                && !player.hasPermission("rp.bypass")) {
                        player.kickPlayer("You are required to accept the resource pack!");
                        Bukkit.broadcast("§3[§dParadisu §bツ§3] §c" + player.getName() + " §cdenied the resource pack and was kicked.\n§cIf this was a mistake, give §a" + player.getName() + " §f§lrp.bypass §cor disable rp-required in config.yml", "rp.bypass");
        }
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
}



    @EventHandler
    public void dubsteplauncher(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            String launchername = "§4§lD§c§lu§6§lb§e§ls§2§lt§a§le§b§lp§3§l§1§l§9§l§d§l§4§l§c§l §4§lR§c§la§6§ly§e§lg§2§lu§a§ln";
            if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(launchername)) {
                player.sendMessage("You right clicked the raygun!");
                //gun mechanics
            }
        }
        return;
    }

    @EventHandler
    public static void ItemShowEvent(AsyncPlayerChatEvent pchat) {
        String msg = pchat.getMessage();
        String name = pchat.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName();
        List<String> lore = pchat.getPlayer().getInventory().getItemInMainHand().getItemMeta().getLore();
        Integer amount = pchat.getPlayer().getInventory().getItemInMainHand().getAmount();
        Material mat = pchat.getPlayer().getInventory().getItemInMainHand().getType();

        msg = msg.replace("[item]", "§f[" + name + "§f]");
//        msg = msg.replace("[item]", "" + displayitem);

        pchat.setMessage(msg);
    } */
 // new stuff
}