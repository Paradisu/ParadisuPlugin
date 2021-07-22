package me.jakedadream.ParadisuPlugin;

import me.jakedadream.ParadisuPlugin.commands.snwcommands;
import me.jakedadream.ParadisuPlugin.events.snwevents;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class paradisumain extends JavaPlugin {

    public static void main(String[] args) {
        System.out.println("[Paradisu] Starting...");
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
        getCommand("tempcmd").setExecutor(new snwcommands());
        getCommand("list").setExecutor(new snwcommands());
        getCommand("findplayercords").setExecutor(new snwcommands());
        getCommand("currenttime").setExecutor(new snwcommands());
        //
        //
     //   getCommand("XYZ").setExecutor(new generalcommands());
        getCommand("setwarp").setExecutor(new warps());
        getCommand("delwarp").setExecutor(new warps());
        getCommand("warp").setExecutor(new warps());
        //
        //
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        createWarpFiles();
        saveWarpConfig();

        //
        //
        getServer().getPluginManager().registerEvents(new luckyblocks(), this);
        getServer().getPluginManager().registerEvents(new entityedits(), this);
        getServer().getPluginManager().registerEvents(new toys(), this);
        getServer().getPluginManager().registerEvents(new chatevents(), this);
        getServer().getPluginManager().registerEvents(new snwevents(), this);
        //
        //
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[Paradisu] Plugin is now enabled");
        //
        //

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
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Plugin is now disabled :(");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Fuck you");
    }


    public static File sourceWarpFile;
    public static FileConfiguration fileWarpConfig;

    public void createWarpFiles() {

        sourceWarpFile = new File(getDataFolder(), "warps.yml");

        if (!sourceWarpFile.exists()) {
            sourceWarpFile.getParentFile().mkdirs();
            saveResource("warps.yml", false);
        }

        fileWarpConfig = new YamlConfiguration();

        try {
            fileWarpConfig.load(sourceWarpFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    //gets fileWarpConfig
    public static FileConfiguration getWarpConfig() {
        return fileWarpConfig;
    }

    //use if edited through commands
    public static void saveWarpConfig(){
        try {
            fileWarpConfig.save(sourceWarpFile);
        } catch (IOException e){
            System.out.println("couldn't save file");
        }
    }

    //use if edited file through text editor
    public static void reloadWarpConfig(){
        fileWarpConfig = YamlConfiguration.loadConfiguration(sourceWarpFile);
    }
}


