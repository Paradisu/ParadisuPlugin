package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;
import org.bukkit.ChatColor;

import static org.bukkit.Bukkit.getServer;


public class DBConnections {

    private static Connection Paradisu_Connection;
    
    public static Connection ParadisuSQLCon() { 
        if(Paradisu_Connection == null){
            try {
                Paradisu_Connect();
            } catch (ClassNotFoundException e) {
               
                e.printStackTrace();
            } catch (SQLException e) {
                
                e.printStackTrace();
            }

        }
        return Paradisu_Connection; 
    }



    public static boolean Paradisu_IsConnected() {
        return (Paradisu_Connection != null);
    }

    public static void Paradisu_Connect() throws ClassNotFoundException, SQLException {
        if (!Paradisu_IsConnected()) {
            Dotenv dotenv_load = Dotenv.load();

            String jdbc = dotenv_load.get("Paradisu_JDBC");
            String username = dotenv_load.get("Paradisu_USERNAME");
            String password = dotenv_load.get("Paradisu_PASSWORD");

            Paradisu_Connection = DriverManager.getConnection(jdbc, username, password);
            
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Paradisu] Now connected to the Database");

        }
    }

    public static void Paradius_Disconnect() {
        if (Paradisu_IsConnected()) {
            try {
                Paradisu_Connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
