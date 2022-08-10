package net.paradisu.paradisuplugin.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.DefaultContextKeys;

import java.util.Calendar;

import static org.bukkit.Bukkit.getServer;

public class TimeZone {

    public static void setAnyTime(String timezone) {

        Calendar calTimezone = Calendar.getInstance();
        calTimezone.setTimeZone(java.util.TimeZone.getTimeZone(timezone));
        long Timezonehour = calTimezone.get(Calendar.HOUR_OF_DAY);
        double Timezoneminute = calTimezone.get(Calendar.MINUTE);
        double Timezoneseconds = calTimezone.get(Calendar.SECOND);

        long Timezonehourtick;
        if (Timezonehour >= 6) { Timezonehourtick = (Timezonehour * 1000) - 6000; } else {
            Timezonehourtick = (Timezonehour * 1000) + 18000; }
        double Timezoneminsec = (Timezoneminute * 60d) + Timezoneseconds;
        double Timezonesecondstick = (Timezoneminsec/3600d) * 1000d;
        long Timezonetimeticks = (long) (Timezonesecondstick + Timezonehourtick);

        World overworld = Bukkit.getWorlds().get(0);
        getServer().getWorld(overworld.getName()).setTime(Timezonetimeticks);

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[Paradisu] We succesfully set the time to " + Timezonetimeticks);

    }

    public static void serverStartupTimeSetter() {

        String japanTimezone = "Asia/Tokyo";
        String californiaTimezone = "America/Los_Angeles";
        String serverName = LuckPermsProvider.get().getServerName();

        switch(serverName) {
            case "usj":
                setAnyTime(japanTimezone);
                break;

            case "tdr":
                setAnyTime(japanTimezone);
                break;

            case "ush":
                setAnyTime(californiaTimezone);
                break;

            default:
                getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[Paradisu] NO DEFAULT TIME REGISTERED FOR SERVER: "+ DefaultContextKeys.SERVER_KEY);
                break;
        }
    }

    public static String UnixToPlayTime(Long timeProvided) {
        String dateTimeString;
        String Dayss, Hourss, Minutess, Secondss;
        String ds = "", hs = "", mins = "", ss = "";

        Long Days = timeProvided / (24 * 3600); timeProvided = timeProvided % (24 * 3600);
        Long Hours = timeProvided / 3600; timeProvided %= 3600;
        Long Minutes = timeProvided / 60; timeProvided %= 60;
        Long Seconds = timeProvided;

        if (Days != 1) ds = "s";
        if (Hours != 1) hs = "s";
        if (Minutes != 1) mins = "s";
        if (Seconds != 1) ss = "s";
        // ---------------------------------------------------------------------------------- //
        if (Days != 0) { Dayss = Days.toString() + " Day" + ds + ", "; } 
            else { Dayss = ""; }
        if (Hours != 0) { Hourss = Hours.toString() + "" + hs + ", "; } 
            else { Hourss = ""; }
        if (Minutes != 0) { Minutess = Minutes.toString() + " Minute" + mins + ", "; } 
            else { Minutess = ""; }
        if (Seconds != 0) { Secondss = Seconds.toString() + " Second" + ss + ", "; } 
            else { Secondss = ""; }
    
        
        dateTimeString = Dayss + Hourss + Minutess + Secondss;
        return dateTimeString;
    }
}
