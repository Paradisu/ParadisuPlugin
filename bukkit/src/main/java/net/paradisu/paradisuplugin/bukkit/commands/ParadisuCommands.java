package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.BlankItem;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.NoItem;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.ParadisuEffects;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.ParadisuHead;
import net.paradisu.paradisuplugin.bukkit.items.invs.TrashCan;
import net.paradisu.paradisuplugin.bukkit.util.TimeZone;

public class ParadisuCommands {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    @CommandPermission("paradisu.invsee")
    @CommandMethod("invsee|invs <player>")
    public void invsee(CommandSender sender,
            @Argument("player") Player player) {
        Player p = (Player) sender;
        Player target = player;

        Inventory inv = Bukkit.createInventory(null, 54, "§f" + target.getName() + "'s Inventory");
        PlayerInventory playerInv = target.getInventory();
        inv.setContents(playerInv.getContents());
        for (int i = 36; i < 45; i++)
            inv.setItem(i, new BlankItem());
        inv.setItem(49, new BlankItem());
        inv.setItem(46, new ParadisuHead(target));

        // off hand
        if (playerInv.getItemInOffHand() == null)
            inv.setItem(45, new NoItem(" §7(off hand)"));
        else
            inv.setItem(45, playerInv.getItemInOffHand().clone());
        // helmet
        if (playerInv.getHelmet() == null)
            inv.setItem(50, new NoItem(" §7(helmet)"));
        else
            inv.setItem(50, playerInv.getHelmet().clone());
        // chestplate
        if (playerInv.getChestplate() == null)
            inv.setItem(51, new NoItem(" §7(chestplate)"));
        else
            inv.setItem(51, playerInv.getChestplate().clone());
        // leggings
        if (playerInv.getLeggings() == null)
            inv.setItem(52, new NoItem(" §7(leggings)"));
        else
            inv.setItem(52, playerInv.getLeggings().clone());
        // boots
        if (playerInv.getBoots() == null)
            inv.setItem(53, new NoItem(" §7(boots)"));
        else
            inv.setItem(53, playerInv.getBoots().clone());

        inv.setItem(48, new ParadisuEffects(target));

        p.openInventory(inv);
        p.sendMessage(cmdprefix + "§fOpening the inventory of §3" + target.getName() + ".");
    }

    @CommandPermission("paradisu.trashcan")
    @CommandMethod("trashcan|trash")
    public void trash(CommandSender sender) {
        Player p = (Player) sender;
        p.openInventory(new TrashCan().getInventory());
        p.sendMessage(cmdprefix + "§fOpened a trashcan");
    }

    @CommandPermission("paradisu.currenttime")
    @CommandMethod("currenttime")
    public void currentTime(CommandSender sender){
        Player player = (Player) sender;
        Long ct = player.getWorld().getTime();

        // Converting time to Millitary
        long gameTime = ct;
        long hours = gameTime / 1000 + 6;
        hours %= 24;
        if (hours == 24)
            hours = 0;
        long minutes = (gameTime % 1000) * 60 / 1000;
        String mm = "0" + minutes;
        mm = mm.substring(mm.length() - 2, mm.length());

        // Converting to normal time
        long hoursparsed;
        String ampm;
        if (hours > 12) {
            ampm = "PM";
            hoursparsed = hours - 12;
        } else if (hours < 12 && hours > 0) {
            ampm = "AM";
            hoursparsed = hours;
        } else if (hours == 0) {
            hoursparsed = 1;
            ampm = "AM";
        } else if (hours == 12) {
            hoursparsed = 1;
            ampm = "PM";
        } else {
            hoursparsed = 123;
            ampm = "Error";
        }

        player.sendMessage(cmdprefix + "§fThe current time in" + cmdemph + " §lJapan §f§o(ingame)§f is "
                + cmdemph + hoursparsed + "§f:" + cmdemph + mm + " " + ampm + "§f.");
    }

    @CommandPermission("paradisu.time")
    @CommandMethod("syncjapantime|sjt")
    @CommandDescription("Syncs ingame time to japanese time")
    public void syncJapanTime(CommandSender sender) {
        Player player = (Player) sender;
        TimeZone.setAnyTime("Asia/Tokyo");
        player.sendMessage(cmdprefix + "§fWe set the server time to " + cmdemph + "Japanese §ftime.");
    }

    @CommandPermission("paradisu.time")
    @CommandMethod("synctimezone <timezone>")
    @CommandDescription("Syncs ingame time to provided timezone")
    public void syncTimeZone(CommandSender sender,
    @Argument("timezone") String timezone) {
        Player player = (Player) sender;
        try {
            TimeZone.setAnyTime(timezone);
            player.sendMessage(cmdprefix + "§fWe set the server time to " + cmdemph + timezone + " §ftime.");
        } catch (Exception e) {
            player.sendMessage(cmdprefix + "§fTimezone does not exist");
        }
    }

    @CommandPermission("paradisu.stack")
    @CommandMethod("stack <targetOne> [targetTwo]")
    @CommandDescription("Automatically stacks you onto another player & use them as a vehicle")
    public void stack(CommandSender sender,
                      @Argument("targetOne") Player targetOne,
                      @Argument("targetTwo") Player targetTwo
                      ) {
        Player player = (Player) sender;
        if (targetTwo == null) {
            player.addPassenger(targetOne);
            player.sendMessage(cmdprefix + "§fYou are now stacked onto " + cmdemph + targetOne.getName() + "§f.");
        } else {
            targetOne.addPassenger(targetTwo);
            player.sendMessage(cmdprefix + cmdemph + targetTwo.getName() + " §fis now stacked onto " + cmdemph + targetOne.getName() + "§f.");
        }
    }

    @CommandPermission("paradisu.server")
    @CommandMethod("serverswitcher")
    @CommandDescription("Opens an inventory that takes you to a different server")
    public void serverSwitcher(CommandSender sender) {
        // Player player = (Player) sender;

        


    }

}
