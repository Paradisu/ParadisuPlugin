package me.jakedadream.snwplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import sun.misc.MessageUtils;

import java.util.function.Function;

public class snwevents implements Listener {

    @EventHandler
    public static void onPlayerCommand(PlayerJoinEvent welcomemsg) {
        Player player = welcomemsg.getPlayer();
        player.sendMessage("§f§l----------------------------");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fWelcome to §f§lSuper Nintendo World!");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fBe sure to do §e/rp §f§ §e/audio§f!");
        player.sendMessage("§f§l----------------------------");
    }
}