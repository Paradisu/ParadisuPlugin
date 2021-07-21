package me.jakedadream.ParadisuPlugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


       // commands done by console & players


        return true;
    }
}