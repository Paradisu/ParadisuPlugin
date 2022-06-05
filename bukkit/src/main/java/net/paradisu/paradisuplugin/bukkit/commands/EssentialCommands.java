package net.paradisu.paradisuplugin.bukkit.commands;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class EssentialCommands /* MEANT FOR ANY REWRITTEN VANILLA/QOL COMMANDS */ {

    public EssentialCommands(){

    }

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Specificly sets your gamemode to requested gamemode")
    @CommandMethod("gm|gamemode <mode> [target]")
    public void gamemodeSet(CommandSender sender,
        @Argument("mode") String gamemode,
        @Argument("target") Player target
    ) {
        Player player;
        if (target != null) {
            player = target;
        } else {
            player = (Player) sender;
        }

        switch (gamemode){
            case "s", "0", "survival" -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(cmdprefix + "§fYour gamemode has been set to §6Survival§f.");
            }
            case "c", "1", "creative" -> {
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage(cmdprefix + "§fYour gamemode has been set to §6Creative§f.");
            }
            case "a", "2", "adventure" -> {
                player.setGameMode(GameMode.ADVENTURE);
                player.sendMessage(cmdprefix + "§fYour gamemode has been set to §6Adventure§f.");
            }
            case "sp", "3", "spectator" -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage(cmdprefix + "§fYour gamemode has been set to §6Spectator§f.");
            }
            default -> player.sendMessage(cmdprefix + "§fInvalid gamemode.");
        }
    }

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Sets your ingame gamemode to Creative")
    @CommandMethod("gmc|gm1")
    public void gamemodeCreative(CommandSender sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(cmdprefix + "§fYour gamemode has been set to " + cmdemph + "Creative§f.");
    }

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Sets your ingame gamemode to Survival")
    @CommandMethod("gms|gm0")
    public void gamemodeSurvival(CommandSender sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(cmdprefix + "§fYour gamemode has been set to " + cmdemph + "Survival§f.");
    }

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Sets your ingame gamemode to Adventure")
    @CommandMethod("gma|gm2")
    public void gamemodeAdventure(CommandSender sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(cmdprefix + "§fYour gamemode has been set to " + cmdemph + "Adventure§f.");
    }

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Sets your ingame gamemode to Spectator")
    @CommandMethod("gmsp|gm3")
    public void gamemodeSpectator(CommandSender sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(cmdprefix + "§fYour gamemode has been set to " + cmdemph + "Spectator§f.");
    }


    @CommandPermission("paradisu.time")
    // @ProxiedBy("time day")
    @CommandMethod("day")
    public void day(CommandSender sender){
        Player player = (Player) sender;
        player.getWorld().setTime(1000);
        player.sendMessage(cmdprefix + "§fYou set the time to " + cmdemph + "Day§f.");
    }

    @CommandPermission("paradisu.time")
    // @ProxiedBy("time night")
    @CommandMethod("night")
    public void night(CommandSender sender){
        Player player = (Player) sender;
        player.getWorld().setTime(14000);
        player.sendMessage(cmdprefix + "§fYou set the time to " + cmdemph + "Night§f.");
    }

    @CommandPermission("paradisu.time")
    // @ProxiedBy("time noon")
    @CommandMethod("noon")
    public void noon(CommandSender sender){
        Player player = (Player) sender;
        player.getWorld().setTime(600);
        player.sendMessage(cmdprefix + "§fYou set the time to " + cmdemph + "Noon§f.");
    }

    @CommandPermission("paradisu.ec")
    @CommandMethod("ec|enderchest|echest")
    public void enderChest(CommandSender sender) {
        Player player = (Player) sender;
        player.openInventory(player.getEnderChest());
    }

    @CommandPermission("paradisu.wb")
    @CommandMethod("wb|workbench")
    public void workbench(CommandSender sender) {
        Player player = (Player) sender;
        player.openWorkbench(null, true);
    }

    @CommandPermission("paradisu.clearinv.own")
    @CommandMethod("clearinv|clear|clearinventory")
    public void clearInv(CommandSender sender) {
        Player p = (Player) sender;
        p.getInventory().clear();
        p.sendMessage(cmdprefix + "§fYour inventory has been cleared.");
    }

    @CommandPermission("paradisu.clearinv.other")
    @CommandMethod("clearinv|clear|clearinventory <player>")
    public void clearInvOther(CommandSender sender,
                              @Argument("player") Player player) {
        Player target = player;
        target.getInventory().clear();
        target.sendMessage(cmdprefix + "§fYour inventory has been cleared.");
    }

    @CommandPermission("paradisu.fly.own")
    @CommandMethod("fly")
    public void fly(CommandSender sender) {
        Player target = (Player) sender;
        if (target.isFlying()) {
            target.setAllowFlight(false);
            target.setFlying(false);
            target.sendMessage(cmdprefix + "§fYou have stopped flying.");
        } else {
            target.setAllowFlight(true);
            target.sendMessage(cmdprefix + "§fYou have started flying.");
        }
    }

    @CommandPermission("paradisu.fly.other")
    @CommandMethod("fly <player>")
    public void flyOther(CommandSender sender,
                         @Argument("player") Player player) {
        Player target = player;
        if (target.isFlying()) {
            target.setAllowFlight(false);
            target.setFlying(false);
            target.sendMessage(cmdprefix + "§fYou have stopped flying.");
        } else {
            target.setAllowFlight(true);
            target.sendMessage(cmdprefix + "§fYou have started flying.");
        }
    }

}

