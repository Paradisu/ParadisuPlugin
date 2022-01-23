package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;
import org.bukkit.ChatColor;

import static org.bukkit.Bukkit.getServer;


public class DBConnections {


    Dotenv dotenv_load = Dotenv.load();

    private Connection Paradisu_Connection;

    public boolean Paradisu_IsConnected() {
        return (Paradisu_Connection != null);
    }

    public void Paradisu_Connect() throws ClassNotFoundException, SQLException {
        if (!Paradisu_IsConnected()) {

            String jdbc = dotenv_load.get("PLAYER_DATA_ENDPOINT");
            String username = dotenv_load.get("PLAYER_DATA_USERNAME");
            String password = dotenv_load.get("PLAYER_DATA_PASSWORD");

            Paradisu_Connection = DriverManager.getConnection(jdbc, username, password);

            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Paradisu] Now connected to the Player Data DB");

        }
    }
}
