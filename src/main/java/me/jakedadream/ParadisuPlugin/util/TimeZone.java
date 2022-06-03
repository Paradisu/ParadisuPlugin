package me.jakedadream.ParadisuPlugin.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.Calendar;

import static org.bukkit.Bukkit.getServer;

public class TimeZone {

    public static void setJapanTime() {

        Calendar calTokyo = Calendar.getInstance();
        calTokyo.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Tokyo"));
        long tokyohour = calTokyo.get(Calendar.HOUR_OF_DAY);
        double tokyominute = calTokyo.get(Calendar.MINUTE);
        double tokyoseconds = calTokyo.get(Calendar.SECOND);

        long tokyohourtick;
        if (tokyohour >= 6) { tokyohourtick = (tokyohour * 1000) - 6000; } else {
            tokyohourtick = (tokyohour * 1000) + 18000; }
        double tokyominsec = (tokyominute * 60d) + tokyoseconds;
        double tokyosecondstick = (tokyominsec/3600d) * 1000d;
        long tokyotimeticks = (long) (tokyosecondstick + tokyohourtick);

        World overworld = Bukkit.getWorlds().get(0);
        getServer().getWorld(overworld.getName()).setTime(tokyotimeticks);

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Paradisu] We succesfully set the time to " + tokyotimeticks);

    }

    public static void setAnyTime(String timezone) {

        Calendar calTokyo = Calendar.getInstance();
        calTokyo.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        long tokyohour = calTokyo.get(Calendar.HOUR_OF_DAY);
        double tokyominute = calTokyo.get(Calendar.MINUTE);
        double tokyoseconds = calTokyo.get(Calendar.SECOND);

        long tokyohourtick;
        if (tokyohour >= 6) { tokyohourtick = (tokyohour * 1000) - 6000; } else {
            tokyohourtick = (tokyohour * 1000) + 18000; }
        double tokyominsec = (tokyominute * 60d) + tokyoseconds;
        double tokyosecondstick = (tokyominsec/3600d) * 1000d;
        long tokyotimeticks = (long) (tokyosecondstick + tokyohourtick);

        World overworld = Bukkit.getWorlds().get(0);
        getServer().getWorld(overworld.getName()).setTime(tokyotimeticks);

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Paradisu] We succesfully set the time to " + tokyotimeticks);

    }

}
