package me.jakedadream.ParadisuPlugin.commands;

import me.jakedadream.ParadisuPlugin.items.ItemManager;
import me.jakedadream.ParadisuPlugin.items.PluginInventories;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class snwcommands implements CommandExecutor {


    private String getParsedName(String[] args) {
        String name = "";

        // Concat all the args to a string
        for (int i = 0; i < args.length; i++) {
            name = name.concat(args[i]);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        short id;

        switch (cmd.getName().toLowerCase()) {


            case "givecoin":
                if (player.hasPermission("snw.givecoin")) {
                    player.getInventory().addItem(ItemManager.createCoin());
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "givestarcoin":
                if (player.hasPermission("snw.givestarcoin")) {
                    player.getInventory().addItem(ItemManager.createStarCoin());
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "sc":
                if (player.hasPermission("snw.sc")) {
                    if (args.length == 0) {
                        // /sc (no args)
                        player.sendMessage("§3[§dParadisu §bツ§3] §fIncorrect usage; Please use '/sc <message>'");
                    } else {
                        String allArgs = "";

                        for (String arg : args) {
                            allArgs += arg + " ";
                        }
                        for (World w : Bukkit.getWorlds()) {
                            for (Player p : w.getPlayers()) {
                                if (p.hasPermission("snw.sc")) {
                                    p.sendMessage("§3『§b§l§oSC§3』 §c" + player.getDisplayName() + " §f»§3 " + allArgs);
                                    // /sc <message>;
                                }
                            }
                        }

                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "ac":
                if (player.hasPermission("snw.ac")) {
                    if (args.length == 0) {
                        // /ac (no args)
                        player.sendMessage("§3[§dParadisu §bツ§3] §fIncorrect usage; Please use '/ac <message>'");
                    } else {
                        String allArgs = "";

                        for (String arg : args) {
                            allArgs += arg + " ";
                        }
                        for (World w : Bukkit.getWorlds()) {
                            for (Player p : w.getPlayers()) {
                                if (p.hasPermission("snw.ac")) {
                                    p.sendMessage("§c『§4§l§oAC§c』 §c" + player.getDisplayName() + " §f»§3 " + allArgs);
                                    // /AC <message>;
                                }
                            }
                        }

                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "gmc":
                if (player.hasPermission("snw.gmc") || player.hasPermission("snw.gm.*")) {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYour gamemode has been set to §3Creative§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "gms":
                if (player.hasPermission("snw.gms") || player.hasPermission("snw.gm.*")) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYour gamemode has been set to §3Survival§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "gmsp":
                if (player.hasPermission("snw.gmsp") || player.hasPermission("snw.gm.*")) {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYour gamemode has been set to §3Spectator§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "gma":
                if (player.hasPermission("snw.gma") || player.hasPermission("snw.gm.*")) {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYour gamemode has been set to §3Adventure§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "enderchest":
                if (player.hasPermission("snw.ec")) {
                    player.openInventory(player.getEnderChest());
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "workbench":
                if (player.hasPermission("snw.wb")) {
                    player.openWorkbench(null, true);
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "invsee":
                if (player.hasPermission("snw.invsee")) {

                    if (args.length == 0) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fIncorrect usage; Please use '/invsee <player>'");
                    }
                    if (args.length >= 1) {
                        Player target = Bukkit.getPlayerExact(args[0]);
                        try {
                            Inventory targetinv = target.getInventory();
                            player.openInventory(targetinv);
                            player.sendMessage("§3[§dParadisu §bツ§3] §fNow opening the inventory of §3" + target.getName() + "");
                        } catch (NullPointerException e) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fThis player does not exist or is offline.");
                        }
                    }

                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");}
                return true;


            case "day":
                if (player.hasPermission("snw.time")) {
                    player.getWorld().setTime(1000);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYou set the time to §3§nDay§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "night":
                if (player.hasPermission("snw.time")) {
                    player.getWorld().setTime(14000);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYou set the time to §3§nNight§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "noon":
                if (player.hasPermission("snw.time")) {
                    player.getWorld().setTime(600);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYou set the time to §3§nNoon§f!");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "spawn":
                if (player.hasPermission("snw.spawn")) {
                    player.performCommand("warp spawn");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "sex":
                if (player.hasPermission("snw.sex")) {
                    player.sendMessage("§3[§dParadisu §bツ§3] §fYou are now having sex!!!!!");
                } else { player.sendMessage("Unknown command. Tyle \"/help\" for help."); }
                return true;

            case "tphere":
                if (player.hasPermission("snw.tphere") || player.hasPermission("snw.tp.*")) {

                    if (args.length == 1) {
                        Player target = Bukkit.getPlayerExact((args[0]));
                        target.teleport((player.getLocation()));
                        player.sendMessage("§3[§dParadisu §bツ§3] §fSuccessfully teleported §3" + target + "§fto you.");

                    } else {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease do /tphere (player)");
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "tp":
                if (player.hasPermission("snw.tp") || player.hasPermission("snw.tp.*")) {
                    if (args.length < 1) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease do §3§o/tp User §f§lOR");
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease do §3§o/tp User User");

                        // TP TO A SINGLE PLAYER
                    } else if (args.length == 1) {
                        Player target = Bukkit.getPlayerExact((args[0]));

                        try {
                            player.teleport((target.getLocation()));
                            player.sendMessage("§3[§dParadisu §bツ§3] §fSuccessfully teleported to§3 " + args[0] + "§f.");
                        } catch (NullPointerException e) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fThis player does not exist or is offline.");
                        }

                        // TP ANOTHER PLAYER TO ANOTHER PLAYER
                    }
                    if (args.length == 2) {
                        Player PlayerToSend = Bukkit.getPlayerExact(args[0]);
                        Player target = Bukkit.getPlayerExact(args[1]);

                        try {
                            PlayerToSend.teleport(target.getLocation());
                            player.sendMessage("§3[§dParadisu §bツ§3] §fSuccessfully teleported §3" + PlayerToSend + "§fto §3" + target + "§f.");
                        } catch (NullPointerException e) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fOne of these players do not exist or are offline.");
                        }
                        // TOO MANY ARGS
                    } else if (args.length > 2) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease do `/tp (player)` OR `/tp (player1) (player2)`");
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "skull":
                if (player.hasPermission("snw.skull")) {
                    if (args.length == 1) {

                        String owner = args[0];
                        ItemStack itemSkull = new ItemStack(Material.PLAYER_HEAD, 1);
                        SkullMeta metaSkull = null;
                        metaSkull = (SkullMeta) itemSkull.getItemMeta();
                        ItemMeta meta = itemSkull.getItemMeta();
                        metaSkull.setOwner(owner);
                        meta.setDisplayName("§7§lSkull of §3" + owner + "§f!");

                        itemSkull.setItemMeta(metaSkull);
                        player.getInventory().addItem(itemSkull);

                        player.sendMessage("§3[§dParadisu §bツ§3] §fWe successfully gave you the head of §3§n" + owner + "§f!");

                    } else {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease provide a valid name!");
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "clearinventory":

                if (player.hasPermission("snw.clearinventory")) {

                    if (args.length == 0) {

                        player.getInventory().clear();
                        player.sendMessage("§3[§dParadisu §bツ§3] §fYour inventory has been cleared.");
                    } else if (args.length == 1 && player.hasPermission("snw.clearinventoryother")) {
                        String cleartarget = args[0];
                        Player target = Bukkit.getServer().getPlayer(cleartarget);
                        target.getInventory().clear();

                        target.sendMessage("§3[§dParadisu §bツ§3] §fYour inventory has been cleared by another player.");
                        player.sendMessage("§3[§dParadisu §bツ§3] §fYou cleared the inventory of§3 " + cleartarget + " §f!");
                    } else if (args.length > 1) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease provide a single valid name!");
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "trashcan":
                if (player.hasPermission("snw.trashcan")) {
                    PluginInventories inv = new PluginInventories();
                    inv.TrashCanInv(player);
                    player.sendMessage("§3[§dParadisu §bツ§3] §fOpened a trashcan");
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "fly":
                if (player.hasPermission("snw.fly")) {
                    if (args.length < 1) {
                        if (player.isFlying()) {
                            player.setAllowFlight(false);
                            player.setFlying(false);

                            player.sendMessage("§3[§dParadisu §bツ§3] §fYou are no longer flying");

                        } else if (!player.isFlying()) {
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            player.sendMessage("§3[§dParadisu §bツ§3] §fYou are now flying");

                        } else {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fHow can you be flying and not flying?");
                        }

                        // FLY OTHER
                        // FLY OTHER
                        // FLY OTHER
                    } else if (args.length >= 1 && player.hasPermission("snw.flyother")) {
                        String flytarget = args[0];
                        Player target = Bukkit.getServer().getPlayer(flytarget);

                        if (target.isFlying()) {
                            player.setAllowFlight(false);
                            target.setFlying(false);

                            player.sendMessage("§3[§dParadisu §bツ§3] §3" + flytarget + " §fis no longer flying!");
                            target.sendMessage("§3[§dParadisu §bツ§3] §fYou are no longer flying");

                        } else if (!player.isFlying()) {
                            player.setAllowFlight(true);
                            target.setFlying(true);

                            player.sendMessage("§3[§dParadisu §bツ§3] §3" + flytarget + " §fis now flying!");
                            target.sendMessage("§3[§dParadisu §bツ§3] §fYou are now flying");

                        } else {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fIf you see this message, the game things you/the player is somehow Flying & Not Flying, please contact Jakey");
                        }
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "rename":
                if (player.hasPermission("snw.rename")) {
                    if (args.length == 0) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease provide arguments");
                    } else {
                        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §7Please hold an item.");
                        } else {

                            ItemStack item = player.getInventory().getItemInMainHand();
                            ItemMeta meta = item.getItemMeta();
                            String name = getParsedName(args);
                            meta.setDisplayName(name);
                            item.setItemMeta(meta);
                        }
                    }

                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;


            case "srename":
                if (player.hasPermission("snw.shulkerrename")) {
                    if (args.length == 0) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease provide a name for your storage");
                    } else {
                        Material phand = player.getInventory().getItemInMainHand().getType();
                        if (phand == Material.AIR || phand == null) {
                            player.sendMessage("§3[§dParadisu §bツ§3] §7Please hold a storage item to rename.");
                        } else if (phand == Material.SHULKER_BOX || phand == Material.WHITE_SHULKER_BOX || phand == Material.LIGHT_GRAY_SHULKER_BOX ||
                                phand == Material.GRAY_SHULKER_BOX || phand == Material.BLACK_SHULKER_BOX || phand == Material.BROWN_SHULKER_BOX ||
                                phand == Material.RED_SHULKER_BOX || phand == Material.ORANGE_SHULKER_BOX || phand == Material.YELLOW_SHULKER_BOX ||
                                phand == Material.LIME_SHULKER_BOX || phand == Material.GREEN_SHULKER_BOX || phand == Material.CYAN_SHULKER_BOX ||
                                phand == Material.LIGHT_BLUE_SHULKER_BOX || phand == Material.BLUE_SHULKER_BOX || phand == Material.PURPLE_SHULKER_BOX ||
                                phand == Material.MAGENTA_SHULKER_BOX || phand == Material.PINK_SHULKER_BOX) {

                                        ItemStack item = player.getInventory().getItemInMainHand();
                                        ItemMeta meta = item.getItemMeta();
                                        String name = getParsedName(args);
                                        meta.setDisplayName(name);
                                        item.setItemMeta(meta);

                            player.sendMessage("§3[§dParadisu §bツ§3] §fYou renamed your storage item to; " + getParsedName(args));


                        } else {
                            player.sendMessage("§3[§dParadisu §bツ§3] §fPlease hold a storage item to rename.");
                        }
                    }
                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "glow":
                if (player.hasPermission("snw.glow")) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() == 0 || player.getInventory().getItemInMainHand().getType() == null) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §7Please hold an item.");
                    } else {

                        ItemStack item = player.getInventory().getItemInMainHand();
                        ItemMeta meta = item.getItemMeta();
                        meta.addEnchant(Enchantment.ARROW_INFINITE, 4341, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        item.setItemMeta(meta);

                        player.sendMessage("§3[§dParadisu §bツ§3] §fItem is now glowing");
                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;



            case "unglow":
                if (player.hasPermission("snw.glow")) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() == 0 || player.getInventory().getItemInMainHand().getType() == null) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §7Please hold an item.");
                    } else {

                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta meta = item.getItemMeta();
                    meta.removeEnchant(Enchantment.ARROW_INFINITE);
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);

                    player.sendMessage("§3[§dParadisu §bツ§3] §fItem is no longer glowing");

                    }
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "broadcast":
                if (player.hasPermission("snw.broadcast")) {
                    if (args.length == 0) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fIncorrect usage; Please use '/broadcast <message>'");
                    } else {
                        String allArgs = "";

                        for (String arg : args) {
                            allArgs += arg + " ";
                        }
                        for (World w : Bukkit.getWorlds()) {
                            for (Player p : w.getPlayers()) {
                                    p.sendMessage((""));
                                    p.sendMessage("§3§l[§dParadisu Broadcast §bツ§3§l] §f§l» " + allArgs);
                                    p.sendMessage((""));
                            }
                        }

                    }
                } else {
                    player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command.");
                }
                return true;

            case "speed":
                if (player.hasPermission("snw.speed")) {
                    if (args.length == 0 || args[0].equalsIgnoreCase("1")) {
                            player.setFlySpeed(0.2F);
                            player.setWalkSpeed(0.2F);
                            player.sendMessage("§3[§dParadisu §bツ§3] §fReset player speed");
                        return true;
                    }
                    if (Float.parseFloat(args[0]) <= 10.0F) {
                            player.setFlySpeed(0.1F * Float.parseFloat(args[0]));
                            player.setWalkSpeed(0.1F * Float.parseFloat(args[0]));
                            player.sendMessage("§3[§dParadisu §bツ§3] §fSet player speed to " + args[0]);
                        return true;
                    }
                        player.sendMessage("§3[§dParadisu §bツ§3] §3" + args[0] + "§f is either higher than 10, or an invalid argument");
                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "sudo":
                if (player.hasPermission("snw.sudo")) {
                    if (args.length < 2) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fPlease use the command as §3/sudo <player> <command or message>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0].toLowerCase());
                    if (target == null) {
                        player.sendMessage("§3[§dParadisu §bツ§3] §fThat player is not online!");
                        return true;
                    }  if (args[1] != null) {
                        StringBuilder execution = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            execution.append(" ").append(args[i]);
                        }
                        if (execution.toString().trim().startsWith("c:")) {
                            target.chat(execution.toString().trim().replace("c:", ""));
                            player.sendMessage("§3[§dParadisu §bツ§3] §fForcing §3" + target.getDisplayName() + " §rto say §3" + execution.toString().trim().replace("c:", ""));
                            return true;
                        }
                        player.getServer().dispatchCommand((CommandSender)target, execution.toString().trim());
                        player.sendMessage("§3[§dParadisu §bツ§3] §fForcing §3" + target.getDisplayName() + " §rto run §3" + execution.toString().trim());
                        return true;
                    }
                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "whomademe":
                if (player.hasPermission("displayname.default")) {
                    player.sendMessage("");
                    player.sendMessage("           §3[§dParadisu §bツ§3] Plugin");
                    player.sendMessage("§c====================================");
                    player.sendMessage("§dPlugin Coded by §3Jakey §d- §3Jakey#9999");
                    player.sendMessage("§dPlugin Coded by §3Cyto §d- §3cyto ツ#7288");
                    player.sendMessage("");
                    player.sendMessage("§b§oWith help from:");
                    player.sendMessage("§3RealInstantRamen §c- §f§oTrashcans & Model ID system");
                    player.sendMessage("§3Andyinnie §c- §f§oEarly coin development");
                    player.sendMessage("§3Kastle_ §c- §f§oPointing out dumb flaws i've made");
                    player.sendMessage("");

                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

                /*
                case "resetluckyblockleaderboard"
                if (player.hasPermission("snw.resetlb")) {
                    getConfig().getConfigurationSection("playerdata");
                    player.sendMessage("§3[§dParadisu §bツ§3] §fReset all lucky block amounts"));
                    for (String key : getConfig().getConfigurationSection("playerdata").getKeys(false)) {
                        String lb = key + ".lb";
                        getConfig().getConfigurationSection("playerdata").set(lb, Integer.valueOf(0));
                    }
                    saveConfig();
                } */


            case "list":
                if (player.hasPermission("snw.list")) {

                    String owners = "";
                    String devs = "";
                    String builders = "";
                    String staff = "";
                    String supporters = "";
                    String visitors = "";
                    Integer onlineammount = Bukkit.getOnlinePlayers().size();


                    for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {

                        String name = player.getName();

                        name = name.concat(", ");

                        if (player.hasPermission("meta.rank.owner")) {
                            owners = owners.concat(name);
                        } else if (player.hasPermission("meta.rank.dev")) {
                            devs = devs.concat(name);
                        } else if  (player.hasPermission("meta.rank.builders")) {
                            builders = builders.concat(name);
                        } else if (player.hasPermission("meta.rank.staff")) {
                            staff = staff.concat(name);
                        } else if (player.hasPermission("meta.rank.supporters")) {
                            supporters = supporters.concat(name);
                        } else {
                            visitors = visitors.concat(name);
                        }

                    }


                    owners = owners.substring(0, owners.length() -2);
                    devs = devs.substring(0, devs.length() -2);
                    builders = builders.substring(0, builders.length() -2);
                    staff = staff.substring(0, staff.length() -2);
                    supporters = supporters.substring(0, supporters.length() -2);
                    visitors = visitors.substring(0, visitors.length() -2);

                    player.sendMessage("§3§l============= §f" + onlineammount + " §d§oOnline Players §3§l=============\n" +
                            " \n");

                    if (owners.length() != 0) {player.sendMessage("§3\uE006 §d»§f " + owners + "\n");}

                    if (devs.length() != 0) {player.sendMessage("§e\uE002 §d»§f " + devs + "\n");}

                    if (builders.length() != 0) {player.sendMessage("§c\uE001 §d»§f " + builders + "\n");}

                    if (staff.length() != 0) {player.sendMessage("§c\uE007 §d»§f " + staff + "\n");}

                    if (supporters.length() != 0) {player.sendMessage("§d\uE008 §d»§f " + supporters + "\n");}

                    if (visitors.length() != 0) {player.sendMessage("§7\uE00A §d»§f " + visitors + "\n");}

                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "findplayercords":
                if (player.hasPermission("snw.findplayercords")) {
                    if (args.length == 1) {
                        Player target = Bukkit.getPlayerExact(args[0]);
                        Location targetlocation = target.getLocation();
                        Integer tx = targetlocation.getBlockX();
                        Integer ty = targetlocation.getBlockY();
                        Integer tz = targetlocation.getBlockZ();
                        String tw = targetlocation.getWorld().getName();



                        player.sendMessage("§3[§dParadisu §bツ§3] §fThe player§3 " + args[0] + " §fis in §3" + tw + "§f at" +
                                " §3X » §d§o" + tx +
                                " §3Y » §d§o" + ty +
                                " §3Z » §d§o" + tz + "§f.");


                    } else { player.sendMessage("§3[§dParadisu §bツ§3] §fNot enough or too many args."); }
                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;

            case "currenttime":
                if (player.hasPermission("snw.currenttime")) {
                    String ampm = null;
                    Long ct = player.getWorld().getTime();
                    if (ct<6000 || ct>18000) {
                        ampm = "AM";
                    } else { ampm = "PM";}


                    Double ctdouble = ct.doubleValue();


                    Long currenthour = (ct%12000)/1000;
                    Double currentminute = ((ctdouble%1000)/16.6);
              //      String[] currentminutesplit = currentminute.toString().split(".", 1);
              //      String currentminute1 = currentminutesplit[0];

                    DecimalFormat format = new DecimalFormat("#00");


                    player.sendMessage("§3[§dParadisu §bツ§3] §fIt is currently §3" + currenthour + "§f:§3" + format.format(currentminute) + " " + "§f§l" + ampm + "§f.");

            //        player.sendMessage("Current Tick Time: " + ct);
            //        player.sendMessage("Current Hour Time: " + currenthour);
            //        player.sendMessage("Current Minute Time: " + format.format(currentminute));
            //        player.sendMessage("It is currently " + currenthour + ":" + currentminute + " " + ampm);


                } else {player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            default:
                    return false;
                    //complain
        }
    }
}
