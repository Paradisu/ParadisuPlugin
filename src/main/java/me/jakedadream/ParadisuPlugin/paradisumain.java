package me.jakedadream.ParadisuPlugin;

import me.jakedadream.ParadisuPlugin.modelmanager.modelscroller.modelscroller_events;
import me.jakedadream.ParadisuPlugin.commands.snwcommands;
import me.jakedadream.ParadisuPlugin.commands.teleportationcmds;
import me.jakedadream.ParadisuPlugin.commands.warps;
import me.jakedadream.ParadisuPlugin.databaseHandlers.DBCommands;
import me.jakedadream.ParadisuPlugin.databaseHandlers.DBConnections;
import me.jakedadream.ParadisuPlugin.paradisu_protocollib.*;
import me.jakedadream.ParadisuPlugin.events.*;
import me.jakedadream.ParadisuPlugin.modelmanager.HatModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.PropModelInv;
import me.jakedadream.ParadisuPlugin.modelmanager.modelcommands;
import me.jakedadream.ParadisuPlugin.shops.ShopCommands;
import me.jakedadream.ParadisuPlugin.shops.ShopGuis;
import me.jakedadream.ParadisuPlugin.wrappers.PlayerDataEvents;
import me.jakedadream.ParadisuPlugin.wrappers.japantime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;


//uploadtest

public class ParadisuMain extends JavaPlugin {


    /*
     _____                    _ _             _____  _             _
    |  __ \                  | (_)           |  __ \| |           (_)
    | |__) |_ _ _ __ __ _  __| |_ ___ _   _  | |__) | |_   _  __ _ _ _ __
    |  ___/ _` | '__/ _` |/ _` | / __| | | | |  ___/| | | | |/ _` | | '_ \
    | |  | (_| | | | (_| | (_| | \__ \ |_| | | |    | | |_| | (_| | | | | |
    |_|   \__,_|_|  \__,_|\__,_|_|___/\__,_| |_|    |_|\__,_|\__, |_|_| |_|
                                                              __/ |
                                                             |___/
        __                 __      __           ___        ______      __
       / /_  __  __       / /___ _/ /_____     ( _ )      / ____/_  __/ /_____
      / __ \/ / / /  __  / / __ `/ //_/ _ \   / __ \/|   / /   / / / / __/ __ \
     / /_/ / /_/ /  / /_/ / /_/ / ,< /  __/  / /_/  <   / /___/ /_/ / /_/ /_/ /
    /_.___/\__, /   \____/\__,_/_/|_|\___/   \____/\/   \____/\__, /\__/\____/
          /____/                                             /____/        */



    public static String CommandPrefix() { String cmdprefix = "\uE016 "; return cmdprefix; }
    public static String CommandEmph() { String cmdemph = "§x§f§d§d§0§2§3"; return cmdemph; }
    public static String NoPermsMessage() { String nopermsmsg = "\uE016 §fYou do not have permission to use that command."; return nopermsmsg; }
    public static String NoArgsMessage() { String noargsmsg = "\uE016 §fNot enough arguments provided."; return noargsmsg; }
    public static String PlayerCooldownMessage() { String cooldownmsg = "\uE016 §fYou are currently on a §x§f§d§d§0§2§3cooldown§f." ; return cooldownmsg; }

    private static DataSource dataSource;

    @Override
    public void onEnable() {

        
        createEnvFiles();
        saveEnvConfig();

        try {
            dataSource = DBConnections.initParadisuSQLCon();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // =================
        // SNW COMMANDS
        // =================
        getCommand("givecoin").setExecutor(new snwcommands());
        getCommand("givestarcoin").setExecutor(new snwcommands());
        getCommand("sc").setExecutor(new snwcommands());
        getCommand("ac").setExecutor(new snwcommands());
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
        getCommand("skull").setExecutor(new snwcommands());
        getCommand("clearinventory").setExecutor(new snwcommands());
        getCommand("trashcan").setExecutor(new snwcommands());
        getCommand("fly").setExecutor(new snwcommands());
        getCommand("rename").setExecutor(new snwcommands());
        getCommand("srename").setExecutor(new snwcommands());
        getCommand("glow").setExecutor(new snwcommands());
        getCommand("unglow").setExecutor(new snwcommands());
        getCommand("broadcast").setExecutor(new snwcommands());
        getCommand("staffbroadcast").setExecutor(new snwcommands());
        getCommand("adminbroadcast").setExecutor(new snwcommands());
        getCommand("supporterbroadcast").setExecutor(new snwcommands());
        getCommand("permbroadcast").setExecutor(new snwcommands());
        getCommand("speed").setExecutor(new snwcommands());
        getCommand("sudo").setExecutor(new snwcommands());
        getCommand("whomademe").setExecutor(new snwcommands());
        getCommand("list").setExecutor(new snwcommands());
        getCommand("findplayercords").setExecutor(new snwcommands());
        getCommand("currenttime").setExecutor(new snwcommands());
        getCommand("mkill").setExecutor(new snwcommands());
        getCommand("lightblocks").setExecutor(new snwcommands());
        getCommand("syncjapantime").setExecutor(new snwcommands());
        getCommand("synctimezone").setExecutor(new snwcommands());
        getCommand("admininvsee").setExecutor(new snwcommands());
        getCommand("unname").setExecutor(new snwcommands());
        getCommand("stack").setExecutor(new snwcommands());
        getCommand("estack").setExecutor(new snwcommands());
        // =================
        // TPING COMMANDS
        // =================
        getCommand("tphere").setExecutor(new teleportationcmds());
        getCommand("tp").setExecutor(new teleportationcmds());
        getCommand("cordstp").setExecutor(new teleportationcmds());
        //
        // =================
        // WARPS COMMANDS
        // =================
        getCommand("setwarp").setExecutor(new warps());
        getCommand("delwarp").setExecutor(new warps());
        getCommand("warp").setExecutor(new warps());
        getCommand("setalias").setExecutor(new warps());
        getCommand("delalias").setExecutor(new warps());
        getCommand("reloadwarp").setExecutor(new warps());
        getCommand("warps").setExecutor(new warps());
        getCommand("warpdisplay").setExecutor(new warps());
        //
        // =================
        // DB COMMANDS
        // =================
        getCommand("dbreset").setExecutor(new DBCommands());
        getCommand("dbdisconnectrememberthisisreallyunsafedontdoit").setExecutor(new DBCommands());
        getCommand("dbconnect").setExecutor(new DBCommands());
        //
        // =================
        // MODEL COMMANDS
        // =================
        getCommand("mgive").setExecutor(new modelcommands());
        getCommand("hgive").setExecutor(new modelcommands());
        getCommand("mhat").setExecutor(new modelcommands());
        // getCommand("createmodelcfsection").setExecutor(new modelcommands());
        // getCommand("reloadprops").setExecutor(new modelcommands());
        // getCommand("reloadhats").setExecutor(new modelcommands());
        //
        // =================
        // SHOP GUI COMMANDS
        // =================
        //
        //
        //
        getCommand("reloadshops").setExecutor(new ShopCommands());
        getCommand("getshop").setExecutor(new ShopCommands());
        //
        // =================
        // SHOP GUI COMMANDS
        // =================
        //getCommand("entitycontrol").setExecutor(new ProtoLib_Basic_Commands());
        //
        //
        //
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        createWarpFiles();
        saveWarpConfig();

        

        // createPropModelsFiles();
        // savePropModelsConfig();

        // createHatModelsFiles();
        // saveHatModelsConfig();

        createShopGuiFiles();
        saveShopGuiConfig();

        PropModelInv.createInvs();
        HatModelInv.createInvs();

        ShopGuis.initShops();
        //
        //
        //
        japantime.setJapanTime();

        // =================
        // EVENTS
        // =================
        getServer().getPluginManager().registerEvents(new luckyblocks(), this);
        getServer().getPluginManager().registerEvents(new entityedits(), this);
        getServer().getPluginManager().registerEvents(new toys(), this);
        getServer().getPluginManager().registerEvents(new SnwEvents(), this);
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerDataEvents(), this);
        getServer().getPluginManager().registerEvents(new modelscroller_events(), this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "\n" + ChatColor.GREEN +
                "|------------------------------|\n" + ChatColor.GREEN +
                "| [Paradisu] Plugin now Active |\n" + ChatColor.GREEN +
                "|------------------------------|");
        //
        //

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                spinningcoins.spineffect();

            }
        }, 0L, 2L);

    }


    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Plugin is now disabled.");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Paradisu] Goodbye!");
    }


    //
    //
    //
    
    private static File envValues;
    private static FileConfiguration envConfig;
    
    private static File sourceWarpFile;
    private static FileConfiguration fileWarpConfig;

    // public static File sourcePropModelsFile;
    // public static FileConfiguration filePropModelsConfig;

    // public static File sourceHatModelsFile;
    // public static FileConfiguration fileHatModelsConfig;

    private static File sourceShopGuiFile;
    private static FileConfiguration fileShopGuiConfig;

    public void createEnvFiles(){
        envValues = new File(getDataFolder(), "env.yml");
        if (!envValues.exists()) {
            try {
                envValues.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        envConfig = YamlConfiguration.loadConfiguration(envValues);
        Bukkit.getConsoleSender().sendMessage("end of createEnvFiles");

    }


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
        if (fileWarpConfig.getConfigurationSection("aliases") == null){
            fileWarpConfig.createSection("aliases");
        }
    }

    // public void createPropModelsFiles() {

    //     sourcePropModelsFile = new File(getDataFolder(), "propmodels.yml");

    //     if (!sourcePropModelsFile.exists()) {
    //         sourcePropModelsFile.getParentFile().mkdirs();
    //         saveResource("propmodels.yml", false);
    //     }

    //     filePropModelsConfig = new YamlConfiguration();

    //     try {
    //         filePropModelsConfig.load(sourcePropModelsFile);
    //     } catch (IOException | InvalidConfigurationException e) {
    //         e.printStackTrace();
    //     }
    //  //   if (filePropModelsConfig.getConfigurationSection("aliases") == null){
    //  //       filePropModelsConfig.createSection("aliases");
    //  //   }
    // }

    // public void createHatModelsFiles() {

    //     sourceHatModelsFile = new File(getDataFolder(), "hatmodels.yml");

    //     if (!sourceHatModelsFile.exists()) {
    //         sourceHatModelsFile.getParentFile().mkdirs();
    //         saveResource("hatmodels.yml", false);
    //     }

    //     fileHatModelsConfig = new YamlConfiguration();

    //     try {
    //         fileHatModelsConfig.load(sourceHatModelsFile);
    //     } catch (IOException | InvalidConfigurationException e) {
    //         e.printStackTrace();
    //     }
    //     //   if (fileHatModelsConfig.getConfigurationSection("aliases") == null){
    //     //       fileHatModelsConfig.createSection("aliases");
    //     //   }
    // }

    public void createShopGuiFiles(){
        sourceShopGuiFile = new File(getDataFolder(), "shopgui.yml");

        if (!sourceShopGuiFile.exists()) {
            sourceShopGuiFile.getParentFile().mkdirs();
            saveResource("shopgui.yml", false);
        }

        fileShopGuiConfig = new YamlConfiguration();

        try {
            fileShopGuiConfig.load(sourceShopGuiFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }



    //gets fileWarpConfig
    public static FileConfiguration getWarpConfig() {
        return fileWarpConfig;
    }

    public static FileConfiguration getEnvConfig(){
        return envConfig;
    }
    // public static FileConfiguration getPropModelsConfig() {
    //     return filePropModelsConfig;
    // }
    // public static FileConfiguration getHatModelsConfig() {
    //     return fileHatModelsConfig;
    // }
    public static FileConfiguration getFileShopGuiConfig() {return fileShopGuiConfig; }


    //use if edited through commands
    public static void saveWarpConfig(){
        try {
            fileWarpConfig.save(sourceWarpFile);
        } catch (IOException e){
            System.out.println("couldn't save file");
        }
    }


    public static void saveEnvConfig(){
        try {
            envConfig.save(envValues);
        } catch (IOException e){
            System.out.println("couldn't save file");
        }
    }
    // public static void savePropModelsConfig(){
    //     try {
    //         filePropModelsConfig.save(sourcePropModelsFile);
    //     } catch (IOException e){
    //         System.out.println("couldn't save file");
    //     }
    // }

    // public static void saveHatModelsConfig(){
    //     try {
    //         fileHatModelsConfig.save(sourceHatModelsFile);
    //     } catch (IOException e){
    //         System.out.println("couldn't save file");
    //     }
    // }

    public static void saveShopGuiConfig() {
        try {
            fileShopGuiConfig.save(sourceShopGuiFile);
        } catch (IOException e) {
            System.out.println("couldn't save file");
        }
    }

    //use if edited file through text editor
    public static void reloadWarpConfig(){
        fileWarpConfig = YamlConfiguration.loadConfiguration(sourceWarpFile);}

    public static void reloadEnvConfig(){
        envConfig = YamlConfiguration.loadConfiguration(envValues);

    }

    // public static void reloadPropModelsConfig(){
    //     filePropModelsConfig = YamlConfiguration.loadConfiguration(sourcePropModelsFile);}

    // public static void reloadHatModelsConfig(){
    //     fileHatModelsConfig = YamlConfiguration.loadConfiguration(sourceHatModelsFile);}

    public static void reloadShopGuiConfig() { fileShopGuiConfig = YamlConfiguration.loadConfiguration(sourceShopGuiFile); }
    public static DataSource getDBCon(){return dataSource;}
}
