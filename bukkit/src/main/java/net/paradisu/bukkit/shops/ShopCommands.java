package net.paradisu.bukkit.shops;

import net.paradisu.bukkit.ParadisuMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommands implements CommandExecutor {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage(); 

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("no");
            return false;
        }
        Player player = (Player) commandSender;
        switch (command.getName()) {
            case ("reloadshops") -> {
                ParadisuMain.reloadShopGuiConfig();
                ShopGuis.initShops();
            }
            case ("setshop") -> {

            }
            case ("getshop") -> {
                if (strings.length == 1){
                    player.openInventory(ShopGuis.getShops().get(Integer.parseInt(strings[0])));
                }
            }

        }
        return false;
    }
}
