package net.paradisu.ParadisuPlugin.items.models;

import net.paradisu.ParadisuPlugin.ParadisuMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class ModelCommands implements CommandExecutor {


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {

            case "mgive":
                if (player.hasPermission("paradisu.model")) {
                    if (args.length == 0) {
                        player.openInventory(ModelGiveInv.getFirstInv(true));
                        // player.openInventory(PropModelInv.getInvs().get(0));
                        player.sendMessage(cmdprefix + "§fOpening the Catalog of Default Models!");
                    } else if (args.length == 1) {
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
                            return false;
                        }
                        player.getInventory().addItem(ModelItemManager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + args[0] + "§f!");
                    } else if (args.length == 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        PlayerInventory inv = target.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fThey do not have space their your inventory.");
                            return false;
                        }
                        target.getInventory().addItem(ModelItemManager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + args[0] + "§f!");
                    } else {
                        player.sendMessage(cmdprefix + "§fNot Enough or too many args");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;


            case "hgive":
                if (player.hasPermission("paradisu.model")) {

                    if (args.length == 0) {
                        player.openInventory(ModelGiveInv.getFirstInv(false));
                        //player.openInventory(HatModelInv.getInvs().get(0));
                        player.sendMessage(cmdprefix + "§fOpening the Catalog of Hat Models!");
                    } else if (args.length == 1) {
                        PlayerInventory inv = player.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
                            return false;
                        }
                        player.getInventory().addItem(ModelItemManager.createHatModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + args[0] + "§f!");
                    } else if (args.length == 2) {
                        Player target = Bukkit.getPlayerExact(args[1]);
                        PlayerInventory inv = target.getInventory();
                        int firstEmpty = inv.firstEmpty();
                        if (firstEmpty == -1) {
                            player.sendMessage(cmdprefix + "§fThey do not have space their your inventory.");
                            return false;
                        }
                        target.getInventory().addItem(ModelItemManager.createHatModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + args[0] + "§f!");

                    } else {
                        player.sendMessage(cmdprefix + "§fNot Enough or too many args");
                    }
                } else {
                    player.sendMessage(nopermsmsg);
                }
                return true;

            case "mhat":
                if (player.hasPermission("paradisu.model")) {

                    if (args.length == 1) {

                        if (player.getInventory().getHelmet() != null) {
                            // ItemStack[] armor = player.getInventory().getArmorContents();
                            ItemStack helmet = player.getEquipment().getHelmet();
                            ItemMeta hmeta = helmet.getItemMeta();
                            helmet.setItemMeta(hmeta);
                            player.getInventory().addItem(helmet);
                        }

                        player.getInventory().setHelmet(ModelItemManager.createPropModel(Integer.parseInt(args[0])));
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
                            // ItemStack[] armor = target.getInventory().getArmorContents();
                            ItemStack helmet = target.getEquipment().getHelmet();
                            ItemMeta hmeta = helmet.getItemMeta();
                            helmet.setItemMeta(hmeta);
                            target.getInventory().addItem(helmet);
                        }

                        target.getInventory().setHelmet(ModelItemManager.createPropModel(Integer.parseInt(args[0])));
                        player.sendMessage(cmdprefix + "§fWe set the model as their helmet.");
                        target.sendMessage(cmdprefix + "§fYour helmet has been set to a model by an admin.");
                    }

                } else { player.sendMessage(nopermsmsg); }
                return true;
        }
        return false;
    }
}
