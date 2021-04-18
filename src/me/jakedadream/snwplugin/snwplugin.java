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
        getCommand("sc").setExecutor(new snwcommands());
        getCommand("gms").setExecutor(new snwcommands());
        getCommand("gmc").setExecutor(new snwcommands());
        getCommand("gmsp").setExecutor(new snwcommands());
        getCommand("gma").setExecutor(new snwcommands());
        getCommand("nickname").setExecutor(new snwcommands());
        getCommand("enderchest").setExecutor(new snwcommands());
        getCommand("workbench").setExecutor(new snwcommands());
        getCommand("invsee").setExecutor(new snwcommands());
        getCommand("invis").setExecutor(new snwcommands());
        getCommand("uninvis").setExecutor(new snwcommands());
        getCommand("day").setExecutor(new snwcommands());
        getCommand("night").setExecutor(new snwcommands());
        getCommand("noon").setExecutor(new snwcommands());
        getCommand("speed").setExecutor(new snwcommands());
        getCommand("flyspeed").setExecutor(new snwcommands());
        getServer().getPluginManager().registerEvents(new snwevents(),this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[SNW] Plugin is now enabled :D");
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[SNW] Plugin is now disabled :(");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[SNW] Fuck you");
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



