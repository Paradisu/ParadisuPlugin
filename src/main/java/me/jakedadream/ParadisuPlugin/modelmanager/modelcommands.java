package me.jakedadream.ParadisuPlugin.modelmanager;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class modelcommands implements CommandExecutor {

    private String getParsedName(String[] args) {
        String name = "";
        for (String arg : args) {
            name = name.concat(arg);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();
    String nopermsmsg = paradisumain.NoPermsMessage();


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {

            case "mgive":
                if (player.hasPermission("snw.model")) {
                    if (args.length == 0) {
                        player.openInventory(PropModelInv.getInvs().get(0));
                        player.sendMessage(cmdprefix + "§fOpening the Catalog of Default Models!");
                    } else if (args.length == 1) {
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
                            return false;
                        }
                        player.getInventory().addItem(modelitemmanager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + args[0] + "§f!");
                    } else if (args.length == 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        PlayerInventory inv = target.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fThey do not have space their your inventory.");
                            return false;
                        }
                        target.getInventory().addItem(modelitemmanager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + args[0] + "§f!");
                    } else {
                        player.sendMessage(cmdprefix + "§fNot Enough or too many args");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;


            case "hgive":
                if (player.hasPermission("snw.model")) {

                    if (args.length == 0) {
                        player.openInventory(HatModelInv.getInvs().get(0));
                        player.sendMessage(cmdprefix + "§fOpening the Catalog of Hat Models!");
                    } else if (args.length == 1) {
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
                            return false;
                        }
                        player.getInventory().addItem(modelitemmanager.createHatModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + args[0] + "§f!");
                    } else if (args.length == 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        PlayerInventory inv = target.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fThey do not have space their your inventory.");
                            return false;
                        }
                        target.getInventory().addItem(modelitemmanager.createHatModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + args[0] + "§f!");

                    } else {
                        player.sendMessage(cmdprefix + "§fNot Enough or too many args");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;

            case "mhat":
                if (player.hasPermission("snw.model")) {

                    if (args.length == 1) {

                        if (player.getInventory().getHelmet() != null) {
                            ItemStack[] armor = player.getInventory().getArmorContents();
                            ItemStack helmet = player.getEquipment().getHelmet();
                            ItemMeta hmeta = helmet.getItemMeta();
                            helmet.setItemMeta(hmeta);
                            player.getInventory().addItem(helmet);
                        }

                        player.getInventory().setHelmet(modelitemmanager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe set the model as your helmet.");
                    } else if (args.length == 0) {
                        ItemStack[] armor = player.getInventory().getArmorContents();
                        ItemStack swap = armor[3];
                        armor[3] = player.getEquipment().getItemInMainHand();
                        player.getInventory().setArmorContents(armor);
                        player.getInventory().setItemInMainHand(swap);
                        player.sendMessage(cmdprefix + "§fWe set the model as your helmet.");
                    } else if (args.length >= 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);

                        if (target.getInventory().getHelmet() != null) {
                            ItemStack[] armor = target.getInventory().getArmorContents();
                            ItemStack helmet = target.getEquipment().getHelmet();
                            ItemMeta hmeta = helmet.getItemMeta();
                            helmet.setItemMeta(hmeta);
                            target.getInventory().addItem(helmet);
                        }

                        target.getInventory().setHelmet(modelitemmanager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe set the model as their helmet.");
                        target.sendMessage(cmdprefix + "§fYour helmet has been set to a model by an admin.");
                    }

                } else { player.sendMessage(nopermsmsg); }
                return true;


            case "reloadprops":
                if(!(player.hasPermission("snw.model.reload"))){
                    player.sendMessage(nopermsmsg);
                    break;
                }
                paradisumain.reloadPropModelsConfig();
                PropModelInv.createInvs();
                player.sendMessage(cmdprefix + "§fReloaded Props");
                break;

            case "reloadhats":
                if(!(player.hasPermission("snw.model.reload"))){
                    player.sendMessage(nopermsmsg);
                    break;
                }
                paradisumain.reloadHatModelsConfig();
                HatModelInv.createInvs();
                player.sendMessage(cmdprefix + "§fReloaded Hats");
                break;

            case "createmodelcfsection":
                if (player.hasPermission("snw.model")) {
                    //
                    Random rand = new Random(); Integer upperbound = 1000; Integer int_random = rand.nextInt(upperbound);
                    String randstring = int_random.toString();
                    //
                    if (args.length < 1) {
                        player.sendMessage(cmdprefix + "§fPlease add an argument & specify between Props & Hats.");
                    } else if (args.length == 1) {
                        // ------------------------------------------------------------------------------------------------------------------------------------------------
                        if (args[0].equals("prop")) {
                            //
                            paradisumain.getPropModelsConfig().createSection(randstring);
                            ConfigurationSection cs = paradisumain.filePropModelsConfig.getConfigurationSection(randstring);

//                            cs.set("custommodeldata", int_random);

                            cs.set("displayname", "Unset Model Name");
                            cs.set("enchantslot1", "Enchantment.LUCK");
                            cs.set("enchantslot2", "Enchantment.LUCK");
                            cs.set("enchantslot3", "Enchantment.LUCK");

                            cs.set("enchantslot1level", 0);
                            cs.set("enchantslot2level", 0);
                            cs.set("enchantslot3level", 0);

                            cs.set("lore1", "DEFAULT LORE 1");
                            cs.set("lore2", "DEFAULT LORE 2");
                            cs.set("lore3", "DEFAULT LORE 3");
                            cs.set("lore4", "DEFAULT LORE 4");
                            cs.set("lore5", "DEFAULT LORE 5");
                            cs.set("lore6", "DEFAULT LORE 6");
                            cs.set("lore7", "DEFAULT LORE 7");
                            cs.set("lore8", "DEFAULT LORE 8");
                            cs.set("lore9", "DEFAULT LORE 9");
                            cs.set("lore10", "DEFAULT LORE 10");

                            cs.set("unbreakable", false);
                            cs.set("hideunbreakable", false);
                            cs.set("hideenchants", false);
                            //
                            paradisumain.savePropModelsConfig();
                            //
                            player.sendMessage(cmdprefix + "§fNew Model Section created!");
                            //
                        } else if (args[0].equals("hat")) {
                            //
                            paradisumain.getHatModelsConfig().createSection(randstring);
//                            paradisumain.getPropModelsConfig().createSection(sectionname);
//                            ConfigurationSection cs = paradisumain.filePropModelsConfig.getConfigurationSection(sectionname);
                            ConfigurationSection cs = paradisumain.getHatModelsConfig().getConfigurationSection(randstring);
//                            cs.set("custommodeldata", int_random);

                            cs.set("displayname", "Unset Hat Name");
                            cs.set("enchantslot1", "Enchantment.LUCK");
                            cs.set("enchantslot2", "Enchantment.LUCK");
                            cs.set("enchantslot3", "Enchantment.LUCK");

                            cs.set("enchantslot1level", 0);
                            cs.set("enchantslot2level", 0);
                            cs.set("enchantslot3level", 0);

                            cs.set("lore1", "DEFAULT LORE 1");
                            cs.set("lore2", "DEFAULT LORE 2");
                            cs.set("lore3", "DEFAULT LORE 3");
                            cs.set("lore4", "DEFAULT LORE 4");
                            cs.set("lore5", "DEFAULT LORE 5");
                            cs.set("lore6", "DEFAULT LORE 6");
                            cs.set("lore7", "DEFAULT LORE 7");
                            cs.set("lore8", "DEFAULT LORE 8");
                            cs.set("lore9", "DEFAULT LORE 9");
                            cs.set("lore10", "DEFAULT LORE 10");

                            cs.set("unbreakable", false);
                            cs.set("hideunbreakable", false);
                            cs.set("hideenchants", false);
                            //
                            paradisumain.saveHatModelsConfig();
                            //
                            player.sendMessage(cmdprefix + "§fNew hat Section created!");




                            //
                        } else {player.sendMessage(cmdprefix + "§fPlease specify between Props & Hats.");}
                    } else {player.sendMessage(cmdprefix + "§fToo many args.");}
                    // ------------------------------------------------------------------------------------------------------------------------------------------------
                } else { player.sendMessage(nopermsmsg); }
                return true;




            default:
                return false;
        }
        return false;
    }
}
