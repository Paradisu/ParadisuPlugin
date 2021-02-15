package me.jakedadream.snwplugin.commands;

import me.jakedadream.snwplugin.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class snwcommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!"); // Removes console's ability to spawn in a coin
            return true;
        }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("givecoin")) {
            player.getInventory().addItem(ItemManager.createCoin());  // Add coins to inventory
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!"); // Removes console's ability to spawn in a coin
            return true;
        }
        Player playeruser = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("thruwand")) {
            playeruser.getInventory().addItem(ItemManager.createThruWand());  // Add thruwand to inventory
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!"); // Removes console's ability to spawn in a coin
            return true;
        }
        Player playeruser2 = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("llamazbanwand")) {
            playeruser.getInventory().addItem(ItemManager.createLLamazBanWand());  // Add llamazbanwand to inventory
        }
        return true;
    }
}
