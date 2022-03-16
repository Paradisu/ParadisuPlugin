package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import me.jakedadream.ParadisuPlugin.paradisumain;
import me.jakedadream.ParadisuPlugin.wrappers.announcementwrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;

public class DBCommands implements CommandExecutor {

    String cmdprefix = paradisumain.CommandPrefix();
    String cmdemph = paradisumain.CommandEmph();
    String nopermsmsg = paradisumain.NoPermsMessage();
    String noargsmsg = paradisumain.NoArgsMessage();

    private static Connection connection;

    private static void establishConnection() {
        if(connection == null)  connection = DBConnections.ParadisuSQLCon();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }
        Player player = (Player) sender;


        switch (cmd.getName().toLowerCase()) {

            case "dbreset":
                if (player.hasPermission("snw.db")) {

                    if (DBConnections.Paradisu_IsConnected() == true) { // Paradisu is connected

                        DBConnections.Paradius_Disconnect();

                        try {
                            DBConnections.Paradisu_Connect();
                            player.sendMessage(cmdprefix + "§fParadisu reconnected to MySQL.");
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                            player.sendMessage(cmdprefix + "§fParadisu cannot connect to MySQL.");

                        }

                    } else { // Paradisu isn't connected

                        try {
                            DBConnections.Paradisu_Connect();
                            player.sendMessage(cmdprefix + "§fParadisu reconnected to MySQL.");
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                            player.sendMessage(cmdprefix + "§fParadisu cannot connect to MySQL.");

                        }

                    }


                } else { player.sendMessage(nopermsmsg); }

            case "dbdisconnectrememberthisisreallyunsafedontdoit":
                if (player.hasPermission("snw.db")) {

                    if (DBConnections.Paradisu_IsConnected() == true) {
                        DBConnections.Paradius_Disconnect();
                        player.sendMessage(cmdprefix + "§fParadisu disconnected from MySQL.");
                    } else {
                        player.sendMessage(cmdprefix + "§fParadisu is already disconnected from MySQL.");
                    }


                } else { player.sendMessage(nopermsmsg); }



            case "dbconnect":
                if (player.hasPermission("snw.db")) {

                    if (DBConnections.Paradisu_IsConnected() == true) {

                        player.sendMessage(cmdprefix + "§fParadisu is already connected from MySQL.");

                    } else {
                        try {
                            DBConnections.Paradisu_Connect();
                            player.sendMessage(cmdprefix + "§fParadisu reconnected to MySQL.");
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                            player.sendMessage(cmdprefix + "§fParadisu cannot connect to MySQL.");

                        }
                    }



                } else { player.sendMessage(nopermsmsg); }




            default:
                return false;
        }
    }
}
