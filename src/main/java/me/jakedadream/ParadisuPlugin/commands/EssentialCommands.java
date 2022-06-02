package me.jakedadream.ParadisuPlugin.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.ProxiedBy;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.meta.SimpleCommandMeta;
import me.jakedadream.ParadisuPlugin.ParadisuMain;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EssentialCommands {

    private boolean isRealPlayer(CommandSender sender) {
        return sender instanceof Player;
    }


    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    @CommandPermission("paradisu.gamemode")
    @CommandDescription("Specificly sets your gamemode to requested gamemode")
    @CommandMethod("gm|gamemode")
    public void gamemodeSet(CommandExecutor sender,
        @Argument("gamemodeChar") String gamemodeChar,
        @Argument("gamemodeInt") int gamemodeInt
    ) {
        Player player = (Player) sender;

        if (gamemodeChar == "s" || gamemodeInt == 0) {
            player.setGameMode(GameMode.SURVIVAL);
        } else if (gamemodeChar == "c" || gamemodeInt == 1) {
            player.setGameMode(GameMode.CREATIVE);
        } else if (gamemodeChar == "sp" || gamemodeInt == 3) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setGameMode(GameMode.ADVENTURE);
        }
    }


    @CommandPermission("paradisu.gamemode|paradisu.gamemode.c")
    @CommandDescription("Sets your ingame gamemode to Creative")
    @CommandMethod("gmc|gm1")
    public void gamemodeCreative(CommandExecutor sender) {
        Player player = (Player) sender;
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(cmdprefix + "§fYour gamemode has been set to " + cmdemph + "Creative§f.");
    }





    // END
}

