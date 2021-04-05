package me.jakedadream.snwplugin.commands;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class staffchat {

    public boolean onCommand(CommandSender sender, Command sccmd, String scstring, String[] scargs) {
        if (!(sender instanceof Player)) {
            if (sender.hasPermission("snw.sc")) {
                sender.sendMessage(ChatColor.RED + "Only players can use that command!");
                return true;
            }
            Player player = (Player) sender;
            short id;

            switch (sccmd.getName().toLowerCase()) {


                case "sc":
                    player.sendMessage("§3[§c§lS§b§lN§a§lW§3] §fIncorrect usage; Please use '/sc <message>'");
                    return true;


                case "sc2":
                    

            }

        }
        return true;
    }
}