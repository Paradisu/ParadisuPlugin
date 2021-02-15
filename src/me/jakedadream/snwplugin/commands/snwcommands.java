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
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "givecoin":
                player.getInventory().addItem(ItemManager.createCoin());
                return true;
            case "launch":
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Zoooom!");
                if (args.length == 0) {
                    // /launch
                    player.setVelocity(player.getLocation().getDirection().multiply(2).setY(2));
                } else {
                    // /launch <number>
                    player.setVelocity(player.getLocation().getDirection().multiply(Integer.parseInt(args[0])).setY(2));
                }
                return true;
            default:
                return false;
                //complain
        }
    }

}
