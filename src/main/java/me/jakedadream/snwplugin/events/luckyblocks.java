package me.jakedadream.snwplugin.events;

import me.jakedadream.snwplugin.snwplugin;
import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class luckyblocks {


    public void run() {
        /*
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        TimeZone ttz = TimeZone.getTimeZone("GMT+9");
        sdf.setTimeZone(ttz);
        Date rawdate = new Date();
        String date = sdf.format(Long.valueOf(rawdate.getTime()));
        if (date.equals("23:59")) {
            Bukkit.broadcastMessage("§e§LDaily lucky block scores reset");
            snwplugin.this.getConfig().getConfigurationSection("playerdata");
            for (String key : snwplugin.this.getConfig().getConfigurationSection("playerdata").getKeys(false)) {

                String lb = key + ".lb";
                snwplugin.this.getConfig().getConfigurationSection("playerdata").set(lb, Integer.valueOf(0));
                snwplugin.this.saveConfig();
            }
        } */
    }
}