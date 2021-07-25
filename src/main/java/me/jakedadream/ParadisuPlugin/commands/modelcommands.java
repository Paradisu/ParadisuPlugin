package me.jakedadream.ParadisuPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class modelcommands implements CommandExecutor {

    private String getParsedName(String[] args) {
        String name = "";
        for (int i = 0; i < args.length; i++) {
            name = name.concat(args[i]);
            name = name.concat(" ");
        }
        return ChatColor.translateAlternateColorCodes('&', name);
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {

            case "mgive":
                if (player.hasPermission("snw.model")) {

                    // model menu
                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fOpening the Catalog of Default Models!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            case "hgive":
                if (player.hasPermission("snw.model")) {

                    // hat menu
                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fOpening the Catalog of Hat Models!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            case "mhat":
                if (player.hasPermission("snw.model")) {

                    // model on head

                    player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fPlaced the model <display name> on your head!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;


            case "createmodelcfsection":
                if (player.hasPermission("snw.model")) {

                    // create new default model section in config file

                player.sendMessage("§3[§dParadisu §f§lMODELS §bツ§3] §fNew Model Section created!");
                } else { player.sendMessage("§3[§dParadisu §bツ§3] §7You do not have permission to use that command."); }
                return true;




            default:
                return false;
        }
    }
}


