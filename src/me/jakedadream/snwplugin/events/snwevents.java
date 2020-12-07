package me.jakedadream.snwplugin.events;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class snwevents implements Listener {

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent joinEve) {
        Player player = joinEve.getPlayer();
        try {
            Thread.sleep(1500);                //START OF SUPER
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§b§lS", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§b§lS§e§lU", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§b§lS§e§lU§c§lP", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§b§lS§e§lU§c§lP§a§lE", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§b§lS§e§lU§c§lP§a§lE§e§lR", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(300);                //START OF NINTENDO
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "N", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NI", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NIN", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NINT", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NINTE", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NINTEN", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NINTEND", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "NINTENDO", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(300);              //START OF WORLD
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§c§lW", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§c§lW§a§lO", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§c§lW§a§lO§e§lR", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§c§lW§a§lO§e§lR§b§lL", " ", 0, 120, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendTitle(ChatColor.BOLD + "§c§lW§a§lO§e§lR§b§lL§d§lD", " ", 0, 20, 10); //title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0F, 6.0F);  //playsound
        try {
            Thread.sleep(150);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        player.sendMessage("§f§l----------------------------");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fWelcome to §f§lSuper Nintendo World!");
        player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fBe sure to do §e/rp §f§ §e/audio§f!");
        player.sendMessage("§f§l----------------------------");
    }
}