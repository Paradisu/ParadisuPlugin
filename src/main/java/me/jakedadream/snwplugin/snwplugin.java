package me.jakedadream.snwplugin;

import me.jakedadream.snwplugin.commands.snwcommands;
import me.jakedadream.snwplugin.events.snwevents;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import me.jakedadream.snwplugin.events.entityedits;

import java.io.File;
import java.io.IOException;

public class snwplugin extends JavaPlugin {

    public static void main(String[] args) {
        System.out.println("[SNW] Starting...");
    }

    /*
   _____                         _   _ _       _                 _        __          __        _     _
  / ____|                       | \ | (_)     | |               | |       \ \        / /       | |   | |
 | (___  _   _ _ __   ___ _ __  |  \| |_ _ __ | |_ ___ _ __   __| | ___    \ \  /\  / /__  _ __| | __| |
  \___ \| | | | '_ \ / _ \ '__| | . ` | | '_ \| __/ _ \ '_ \ / _` |/ _ \    \ \/  \/ / _ \| '__| |/ _` |
  ____) | |_| | |_) |  __/ |    | |\  | | | | | ||  __/ | | | (_| | (_) |    \  /\  / (_) | |  | | (_| |
 |_____/ \__,_| .__/ \___|_|    |_| \_|_|_| |_|\__\___|_| |_|\__,_|\___/      \/  \/ \___/|_|  |_|\__,_|
              | |
              |_|
.  .   .  .     .  .             .          .---.      .
 \  \ /  /   o _|__|_            |              |      |
  \  \  /.--..  |  |  .-. .--.   |.-. .  .      | .-.  |.-. .-. .  .
   \/ \/ |   |  |  | (.-' |  |   |   )|  |      ;(   ) |-.'(.-' |  |
    ' '  ' -' `-`-'`-'`--''  `-  '`-' `--|  `--'  `-'`-'  `-`--'`--|
                                         ;                         ;
                                      `-'                       `-'

        With help from RealInstantRamen, Andyinnie, & Kastle yelling in my ear.
*/

    int sched;

    @Override
    public void onEnable() {


//        ArrayList createCommand = getCommand(().setExecutor(new snwcommands()));
        getCommand("givecoin").setExecutor(new snwcommands());
        getCommand("givestarcoin").setExecutor(new snwcommands());
        getCommand("mgive").setExecutor(new snwcommands());
        getCommand("mhat").setExecutor(new snwcommands());
        getCommand("idlist").setExecutor(new snwcommands());
        getCommand("sc").setExecutor(new snwcommands());
        getCommand("gmc").setExecutor(new snwcommands());
        getCommand("gms").setExecutor(new snwcommands());
        getCommand("gmsp").setExecutor(new snwcommands());
        getCommand("gma").setExecutor(new snwcommands());
        getCommand("enderchest").setExecutor(new snwcommands());
        getCommand("workbench").setExecutor(new snwcommands());
        getCommand("invsee").setExecutor(new snwcommands());
        getCommand("day").setExecutor(new snwcommands());
        getCommand("night").setExecutor(new snwcommands());
        getCommand("noon").setExecutor(new snwcommands());
        getCommand("spawn").setExecutor(new snwcommands());
        getCommand("sex").setExecutor(new snwcommands());
        getCommand("tphere").setExecutor(new snwcommands());
        getCommand("tp").setExecutor(new snwcommands());
        getCommand("skull").setExecutor(new snwcommands());
        getCommand("clearinventory").setExecutor(new snwcommands());
        getCommand("trashcan").setExecutor(new snwcommands());
        getCommand("fly").setExecutor(new snwcommands());
        getCommand("rename").setExecutor(new snwcommands());
        getCommand("srename").setExecutor(new snwcommands());
        getCommand("glow").setExecutor(new snwcommands());
        getCommand("unglow").setExecutor(new snwcommands());
        getCommand("broadcast").setExecutor(new snwcommands());
        getCommand("speed").setExecutor(new snwcommands());
        getCommand("sudo").setExecutor(new snwcommands());
        getCommand("whomademe").setExecutor(new snwcommands());
        //
        //
        createwarpfiles();
        //
        //
        getServer().getPluginManager().registerEvents(new snwevents(), this);
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[SNW] Plugin is now enabled :D");


     /*











     */


 /*       if (!Bukkit.getScheduler().isCurrentlyRunning(sched)) {
            sched = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    new entityedits().load();
                }
            }, 1, 1);
        } */
    }


    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[SNW] Plugin is now disabled :(");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[SNW] Fuck you");
    }


    private File configf;
    private FileConfiguration warpsconfig;

    public void createwarpfiles() {

        configf = new File(getDataFolder(), "warps.yml");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("warps.yml", false);
        }

        warpsconfig = new YamlConfiguration();

        try {
            warpsconfig.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getWarpsconfig() {
        return this.warpsconfig;
    }
}


