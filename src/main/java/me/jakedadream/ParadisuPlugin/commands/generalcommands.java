package me.jakedadream.ParadisuPlugin.commands;

import me.jakedadream.ParadisuPlugin.paradisumain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class generalcommands implements CommandExecutor {

    private String getParsedName(String[] args) {
        String name = "";

        // Concat all the args to a string
        for (int i = 0; i < args.length; i++) {
            name = name.concat(args[i]);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }
//

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


       // commands done by console & players


        return true;
    }
}