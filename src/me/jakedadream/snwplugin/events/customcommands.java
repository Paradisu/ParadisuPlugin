package me.jakedadream.snwplugin.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class customcommands {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("mgive")) {
            if (sender instanceof Player) {
                // player
                Player player = (Player) sender;
                // /mhat
                if (args.length == 0) {
                    player.sendMessage("§3You need to put an ID Argument!");
                    return true;
                } else {
                    // console
                    sender.sendMessage("You cannot run this command");
                    return true;
                }
                // /mhat <number>
                player.sendMessage("§3You successfully have put a model §a(ID:§c §a)§3 on your head!");
                ItemStack[] armor = {};
                armor[3] = new ItemStack(Material.DIAMOND_AXE)

            }
        }
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("launch")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("*console goes zoom*");
                return true;
            }
            Player player = (Player)  sender;

            if (args.length == 0) {
                // /launch
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Zoooom!");
                player.setVelocity(player.getLocation().getDirection().multiply(2).setY(2));

                return true;
            }

            // /launch <number>
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Zoooom!");
            player.setVelocity(player.getLocation().getDirection().multiply(Integer.parseInt(args[0])).setY(2));

            return true;
        }
        return false;
    }
}





