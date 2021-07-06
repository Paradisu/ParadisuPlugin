package me.jakedadream.snwplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class snwRP implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            /*
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("rp")) {
            if (args.length == 0) {
                if (getConfig().getString("resource-pack-link").length() == 0) {
                    player.sendMessage(getConfig().getString("message-prefix") + " §cError using provided resource link! (IAE or empty)");
                    return true;
                }
                try {
                    player.setResourcePack(getConfig().getString("resource-pack-link"));
                    player.sendMessage(getConfig().getString("message-prefix") + " §rLoading server resource pack...");
                } catch (IllegalArgumentException IAE) {
                    player.sendMessage(getConfig().getString("message-prefix") + " §cError using provided resource link! (IAE or empty)");
                    return true;
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("reload") && ********************snw.rp.reload) {
                reloadConfig();
                saveConfig();
                player.sendMessage(getConfig().getString("message-prefix") + " §rResource config reloaded!");
                return true;
            }

            if (args[0].equalsIgnoreCase("link")) {
                player.sendMessage(getConfig().getString("message-prefix") + " §rHere is the resource pack link: §f" + getConfig().getString("resource-pack-link"));
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(getConfig().getString("message-prefix") + " §rList of commands related to resource pack:\n§2/rp - Loads the resource pack\n§2/rp link - Sends the direct download link for the resource pack\n§2/rp reload - Reloads the resource pack link in the plugin's config\n§2/rp help - Sends this help page for the plugin§r\n" +

                        getConfig().getString("message-prefix") + " §rNotes:\n§2Players with the permission§f§L rp.bypass§r§2 bypass rp restrictions if rp-required is set to true\n§2The resource pack can be set in the config (must be a direct .zip download link)");
                return true;
            }
        } return false;

             */
        return false;
    }
}