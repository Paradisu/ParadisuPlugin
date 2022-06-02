package me.jakedadream.ParadisuPlugin.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.meta.SimpleCommandMeta;
import me.jakedadream.ParadisuPlugin.ParadisuMain;
import me.jakedadream.ParadisuPlugin.warps.WarpCommands;

import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EssentialCommands /* MEANT FOR ANY REWRITTEN VANILLA/QOL COMMANDS */ {

    private boolean isRealPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    public EssentialCommands(){

    }

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Specificly sets your gamemode to requested gamemode")
    @CommandMethod("gm|gamemode <mode>")
    public void gamemodeSet(CommandSender sender,
        @Argument("mode") String gamemode
    ) {
        Player player = (Player) sender;

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
        player.sendMessage(cmdprefix + "§fYou set the time to §3§nDay§f!");
    }

    @CommandPermission("paradisu.time")
    // @ProxiedBy("time night")
    @CommandMethod("night")
    public void night(CommandSender sender){
        Player player = (Player) sender;
        player.getWorld().setTime(14000);
        player.sendMessage(cmdprefix + "§fYou set the time to §3§nNight§f!");
    }

    @CommandPermission("paradisu.time")
    // @ProxiedBy("time noon")
    @CommandMethod("noon")
    public void noon(CommandSender sender){
        Player player = (Player) sender;
        player.getWorld().setTime(600);
        player.sendMessage(cmdprefix + "§fYou set the time to §3§nNoon§f!");
    }

}

