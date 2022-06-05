package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;
import net.paradisu.paradisuplugin.bukkit.util.TimeZone;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.BlankItem;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.NoItem;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.ParadisuEffects;
import net.paradisu.paradisuplugin.bukkit.items.common.menu.ParadisuHead;
import net.paradisu.paradisuplugin.bukkit.items.invs.TrashCan;

public class ParadisuCommands {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

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

    @CommandPermission("paradisu.invsee.admin")
    @ProxiedBy("admininvsee")
    @CommandMethod("invsee admin <player>")
    public void adminInvsee(CommandSender sender,
            @Argument("player") String player) {
        Player p = (Player) sender;
        Player target = Bukkit.getPlayer(player);
        if (target == null) {
            p.sendMessage(cmdprefix + "§fThis player does not exist or is offline.");
            return;
        }
        p.openInventory(target.getInventory());
        p.sendMessage(cmdprefix + "§fOpening the inventory of §3" + target.getName() + ".");
    }

    @CommandPermission("paradisu.trashcan")
    @CommandMethod("trashcan|trash")
    public void trash(CommandSender sender) {
        Player p = (Player) sender;
        p.openInventory(new TrashCan().getInventory());
        p.sendMessage(cmdprefix + "§fOpened a trashcan");
    }

    @CommandPermission("paradisu.list")
    @CommandMethod("list")
    public void list(CommandSender sender) {
        Player player = (Player) sender;
        String owners = "";
        String devs = "";
        String builders = "";
        String staff = "";
        String supporters = "";
        String visitors = "";
        int onlineammount = Bukkit.getOnlinePlayers().size();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String name = p.getName();
            name = name.concat(", ");
            if (p.hasPermission("meta.rank.owner")) owners = owners.concat(name);
            else if (p.hasPermission("meta.rank.dev")) devs = devs.concat(name);
            else if (p.hasPermission("meta.rank.builders")) builders = builders.concat(name);
            else if (p.hasPermission("meta.rank.staff")) staff = staff.concat(name);
            else if (p.hasPermission("meta.rank.supporters")) supporters = supporters.concat(name);
            else visitors = visitors.concat(name);
        }

        if (owners.length() != 0) owners = owners.substring(0, owners.length() - 2);
        if (devs.length() != 0) devs = devs.substring(0, devs.length() - 2);
        if (builders.length() != 0) builders = builders.substring(0, builders.length() - 2);
        if (staff.length() != 0) staff = staff.substring(0, staff.length() - 2);
        if (supporters.length() != 0) supporters = supporters.substring(0, supporters.length() - 2);
        if (visitors.length() != 0) visitors = visitors.substring(0, visitors.length() - 2);

        player.sendMessage("\uE013 " + cmdemph + onlineammount + " §fOnline Players \uE013" + "§r\n");
        if (owners.length() != 0) player.sendMessage("§3\uE006 " + cmdemph + "\ue00d§f " + owners + "\n");
        if (devs.length() != 0) player.sendMessage("§x§f§8§9§9§1§d\uE002 " + cmdemph + "\ue00d§f " + devs + "\n");
        if (builders.length() != 0) player.sendMessage("§x§f§3§6§c§3§6\uE001 " + cmdemph + "\ue00d§f " + builders + "\n");
        if (staff.length() != 0) player.sendMessage("§c\uE007 " + cmdemph + "\ue00d§f " + staff + "\n");
        if (supporters.length() != 0) player.sendMessage("§d\uE008 " + cmdemph + "\ue00d§f " + supporters + "\n");
        if (visitors.length() != 0) player.sendMessage("§7\uE00A " + cmdemph + "\ue00d§f " + visitors + "\n");
    }

    @CommandPermission("paradisu.findplayer")
    @CommandMethod("findplayer|find|findplayercoords <player>")
    public void findPlayer(CommandSender sender,
        @Argument("player") Player playerToFind
    ){
        Player player = (Player) sender;
        Player target = playerToFind;
        if(target == null) {
            player.sendMessage(cmdprefix + "§fPlayer not found.");
            return;
        }
        Location targetlocation = target.getLocation();
        Integer tx = targetlocation.getBlockX();
        Integer ty = targetlocation.getBlockY();
        Integer tz = targetlocation.getBlockZ();
        String tw = targetlocation.getWorld().getName();

        player.sendMessage(cmdprefix + "§fThe player§3 " + playerToFind+ " §fis in §3" + tw + "§f at" +
                " §3X » §d§o" + tx +
                " §3Y » §d§o" + ty +
                " §3Z » §d§o" + tz + "§f.");
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
        TimeZone.setJapanTime();
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

}
