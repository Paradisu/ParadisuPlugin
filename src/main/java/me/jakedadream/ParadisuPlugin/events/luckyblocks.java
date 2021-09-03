package me.jakedadream.ParadisuPlugin.events;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.event.Listener;

public class luckyblocks implements Listener {

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();


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
