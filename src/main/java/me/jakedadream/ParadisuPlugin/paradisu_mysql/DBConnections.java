package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;
import org.bukkit.ChatColor;

import static org.bukkit.Bukkit.getServer;


public class DBConnections {


    Dotenv dotenv_load = Dotenv.load();

    private Connection player_data_connection;
    private Connection warps_connection;
    private Connection shops_connection;

    private static Connection static_player_data_connection;

    public boolean PDisConnected() {
        return (player_data_connection == null ? false : true);
    }

    public boolean WisConnected() {
        return (warps_connection == null ? false : true);
    }

    public boolean SisConnected() {
        return (shops_connection == null ? false : true);
    }

    public static void PDisConnectedStatic() {
        Boolean test = static_player_data_connection == null ? false : true;
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "IS THE DATABASE FOR PLAYERS CONNECTED? (BEFORE) : " + String.valueOf(test));
    }

    public void PLAYER_DATA_connect() throws ClassNotFoundException, SQLException {
        if (!PDisConnected()) {

            String jdbc = dotenv_load.get("PLAYER_DATA_ENDPOINT");
            String username = dotenv_load.get("PLAYER_DATA_USERNAME");
            String password = dotenv_load.get("PLAYER_DATA_PASSWORD");

            player_data_connection = DriverManager.getConnection(jdbc, username, password);



            Boolean test = static_player_data_connection == null ? false : true;
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "IS THE DATABASE FOR PLAYERS CONNECTED? (AFTER) : " + String.valueOf(test));
        }
    }


    public void WARPS_connect() throws ClassNotFoundException, SQLException {
        if (!WisConnected()) {

            String jdbc = dotenv_load.get("WARPS_ENDPOINT");
            String username = dotenv_load.get("WARPS_USERNAME");
            String password = dotenv_load.get("WARPS_PASSWORD");

            warps_connection = DriverManager.getConnection(jdbc, username, password);
        }
    }


    public void SHOPS_connect() throws ClassNotFoundException, SQLException {
        if (!SisConnected()) {

            String jdbc = dotenv_load.get("SHOP_ENDPOINT");
            String username = dotenv_load.get("SHOP_USERNAME");
            String password = dotenv_load.get("SHOP_PASSWORD");

            shops_connection = DriverManager.getConnection(jdbc, username, password);
        }
    }

}
