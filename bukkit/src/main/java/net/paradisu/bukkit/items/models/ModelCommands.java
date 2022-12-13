package net.paradisu.bukkit.items.models;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.paradisu.bukkit.ParadisuMain;
import net.paradisu.bukkit.items.invs.ModelGiveInv;

public class ModelCommands  {


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();


    @CommandPermission("paradisu.model")
    @CommandMethod("mgive")
    public void mGive(CommandSender sender){
        Player p = (Player) sender;
        p.openInventory(new ModelGiveInv(true).getInventory());
    }

    @CommandPermission("paradisu.model")
    @CommandMethod("mgive <model>")
    public void mGiveModel(CommandSender sender,
        @Argument("model") int model
    ){
        Player player = (Player) sender;
        int firstEmpty = player.getInventory().firstEmpty();
        if (firstEmpty == -1) {
            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
            return;
        }
        player.getInventory().addItem(ModelItemManager.createPropModel(model));
        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + model + "§f!");
    }

    @CommandPermission("paradisu.model")
    @CommandMethod("mgive <model> <player>")
    public void mGiveModelPlayer(CommandSender sender,
        @Argument("model") int model,
        @Argument("player") Player player
    ){
        int firstEmpty = player.getInventory().firstEmpty();
        if (firstEmpty == -1) {
            sender.sendMessage(cmdprefix + "§fThey do not have space in their inventory.");
            return;
        }
        player.getInventory().addItem(ModelItemManager.createPropModel(model));
        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + model + "§f!");
        sender.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + model + "§f!");
    }

    @CommandPermission("paradisu.model")
    @CommandMethod("hgive")
    public void hGive(CommandSender sender){
        Player p = (Player) sender;
        p.openInventory(new ModelGiveInv(false).getInventory());
    }

    @CommandPermission("paradisu.model")
    @CommandMethod("hgive <model>")
    public void hGiveModel(CommandSender sender,
        @Argument("model") int model
    ){
        Player player = (Player) sender;
        int firstEmpty = player.getInventory().firstEmpty();
        if (firstEmpty == -1) {
            player.sendMessage(cmdprefix + "§fYou do not have space in your inventory.");
            return;
        }
        player.getInventory().addItem(ModelItemManager.createHatModel(model));
        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + model + "§f!");
    }

    @CommandPermission("paradisu.model")
    @CommandMethod("hgive <model> <player>")
    public void hGiveModelPlayer(CommandSender sender,
        @Argument("model") int model,
        @Argument("player") Player player
    ){
        int firstEmpty = player.getInventory().firstEmpty();
        if (firstEmpty == -1) {
            sender.sendMessage(cmdprefix + "§fThey do not have space in their inventory.");
            return;
        }
        player.getInventory().addItem(ModelItemManager.createHatModel(model));
        player.sendMessage(cmdprefix + "§fWe gave you the model " + cmdemph + "#" + model + "§f!");
        sender.sendMessage(cmdprefix + "§fWe gave them the model " + cmdemph + "#" + model + "§f!");
    }

    // @CommandPermission("paradisu.mhat")
    // @CommandMethod("mhat")
    // public void mHat(CommandSender sender){
    //     Player player = (Player) sender;
    //     ItemStack[] armor = player.getInventory().getArmorContents();
    //     ItemStack swap = armor[3];
    //     armor[3] = player.getEquipment().getItemInMainHand();
    //     player.getInventory().setArmorContents(armor);
    //     player.getInventory().setItemInMainHand(swap);
    //     player.sendMessage(cmdprefix + "§fWe set the model as your helmet.");
    // }
}
