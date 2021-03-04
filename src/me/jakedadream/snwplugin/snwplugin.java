package me.jakedadream.snwplugin;

import me.jakedadream.snwplugin.commands.snwcommands;
import me.jakedadream.snwplugin.events.snwevents;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class snwplugin extends JavaPlugin {

    public static void main(String[] args) {
        System.out.println("Jake is a nerd");
    }

    @Override
    public void onEnable() {
        getCommand("mhat").setExecutor(new snwcommands());
        getCommand("mgive").setExecutor(new snwcommands());
        getCommand("launch").setExecutor(new snwcommands());
        getCommand("givecoin").setExecutor(new snwcommands());
        getCommand("givestarcoin").setExecutor(new snwcommands());
        getCommand("llamazbanwand").setExecutor(new snwcommands());
        getCommand("thruwand").setExecutor(new snwcommands());
        getCommand("idlist").setExecutor(new snwcommands());
        getServer().getPluginManager().registerEvents(new snwevents(),this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[SNW] Plugin is now enabled :D");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[SNW] Plugin is now disabled :(");
    }
}

//    ░█▀▀▀█ ░█─░█ ░█▀▀█ ░█▀▀▀ ░█▀▀█ 　 ░█▄─░█ ▀█▀ ░█▄─░█ ▀▀█▀▀ ░█▀▀▀ ░█▄─░█ ░█▀▀▄ ░█▀▀▀█ 　 ░█──░█ ░█▀▀▀█ ░█▀▀█ ░█─── ░█▀▀▄    ░█▀▀█ ░█─── ░█─░█ ░█▀▀█ ▀█▀ ░█▄─░█
//    ─▀▀▀▄▄ ░█─░█ ░█▄▄█ ░█▀▀▀ ░█▄▄▀ 　 ░█░█░█ ░█─ ░█░█░█ ─░█── ░█▀▀▀ ░█░█░█ ░█─░█ ░█──░█ 　 ░█░█░█ ░█──░█ ░█▄▄▀ ░█─── ░█─░█    ░█▄▄█ ░█─── ░█─░█ ░█─▄▄ ░█─ ░█░█░█
//    ░█▄▄▄█ ─▀▄▄▀ ░█─── ░█▄▄▄ ░█─░█ 　 ░█──▀█ ▄█▄ ░█──▀█ ─░█── ░█▄▄▄ ░█──▀█ ░█▄▄▀ ░█▄▄▄█ 　 ░█▄▀▄█ ░█▄▄▄█ ░█─░█ ░█▄▄█ ░█▄▄▀    ░█─── ░█▄▄█ ─▀▄▄▀ ░█▄▄█ ▄█▄ ░█──▀█


//    ░█▀▄▀█ █▀▀█ █▀▀▄ █▀▀ 　 █▀▀▄ █──█ 　 ───░█ █▀▀█ █─█ █▀▀ ░█▀▀▄ █▀▀█ ░█▀▀▄ █▀▀█ █▀▀ █▀▀█ █▀▄▀█
//    ░█░█░█ █▄▄█ █──█ █▀▀ 　 █▀▀▄ █▄▄█ 　 ─▄─░█ █▄▄█ █▀▄ █▀▀ ░█─░█ █▄▄█ ░█─░█ █▄▄▀ █▀▀ █▄▄█ █─▀─█
//    ░█──░█ ▀──▀ ▀▀▀─ ▀▀▀ 　 ▀▀▀─ ▄▄▄█ 　 ░█▄▄█ ▀──▀ ▀─▀ ▀▀▀ ░█▄▄▀ ▀──▀ ░█▄▄▀ ▀─▀▀ ▀▀▀ ▀──▀ ▀───▀




//                          ░█▀▀█ ─▀─ █▀▀▀ 　 ▀▀█▀▀ █──█ █▀▀█ █▀▀▄ █─█ █▀▀ 　 ▀▀█▀▀ █▀▀█ 　 █▀▀█ █▀▀▄ █▀▀▄ █──█ ─▀─ █▀▀▄ █▀▀▄ ─▀─ █▀▀
//                          ░█▀▀▄ ▀█▀ █─▀█ 　 ──█── █▀▀█ █▄▄█ █──█ █▀▄ ▀▀█ 　 ──█── █──█ 　 █▄▄█ █──█ █──█ █▄▄█ ▀█▀ █──█ █──█ ▀█▀ █▀▀
//                          ░█▄▄█ ▀▀▀ ▀▀▀▀ 　 ──▀── ▀──▀ ▀──▀ ▀──▀ ▀─▀ ▀▀▀ 　 ──▀── ▀▀▀▀ 　 ▀──▀ ▀──▀ ▀▀▀─ ▄▄▄█ ▀▀▀ ▀──▀ ▀──▀ ▀▀▀ ▀▀▀
//
//                               █▀▀█ █▀▀▄ █▀▀▄ 　 ░█▀▀█ █▀▀ █▀▀█ █── ▀█▀ █▀▀▄ █▀▀ ▀▀█▀▀ █▀▀█ █▀▀▄ ▀▀█▀▀ ░█▀▀█ █▀▀█ █▀▄▀█ █▀▀ █▀▀▄
//                               █▄▄█ █──█ █──█ 　 ░█▄▄▀ █▀▀ █▄▄█ █── ░█─ █──█ ▀▀█ ──█── █▄▄█ █──█ ──█── ░█▄▄▀ █▄▄█ █─▀─█ █▀▀ █──█
//                               ▀──▀ ▀──▀ ▀▀▀─ 　 ░█─░█ ▀▀▀ ▀──▀ ▀▀▀ ▄█▄ ▀──▀ ▀▀▀ ──▀── ▀──▀ ▀──▀ ──▀── ░█─░█ ▀──▀ ▀───▀ ▀▀▀ ▀──▀
//




