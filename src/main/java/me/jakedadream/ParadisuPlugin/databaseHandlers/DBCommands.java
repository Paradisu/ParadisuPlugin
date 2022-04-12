package me.jakedadream.ParadisuPlugin.databaseHandlers;

import me.jakedadream.ParadisuPlugin.ParadisuMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DBCommands implements CommandExecutor {

    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();
    String nopermsmsg = ParadisuMain.NoPermsMessage();
    String noargsmsg = ParadisuMain.NoArgsMessage();

    //private static Connection connection;

    

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;

        
        //Connection conn = ParadisuMain.getDBCon().getConnection();
        

        switch (cmd.getName().toLowerCase()) {

            case "dbreset":
                if (player.hasPermission("snw.db")) {

                } else { player.sendMessage(nopermsmsg); }


            case "dbconnect":
                if (player.hasPermission("snw.db")) {

                } else { player.sendMessage(nopermsmsg); }

            default:
                return false;
        }
    }
}
