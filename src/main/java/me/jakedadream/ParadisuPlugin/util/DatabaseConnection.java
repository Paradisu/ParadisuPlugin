package me.jakedadream.ParadisuPlugin.util;

import static org.bukkit.Bukkit.getServer;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.mariadb.jdbc.MariaDbDataSource;
import me.jakedadream.ParadisuPlugin.ParadisuMain;

public class DatabaseConnection {

    private static MariaDbDataSource dataSource;
    
    public static MariaDbDataSource initParadisuSQLCon() throws SQLException { 
        try {
            paradisuConnect();
            testDataSource(dataSource);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSource;
    }



    public static boolean Paradisu_IsConnected() {
        return (dataSource != null);
    }

    public static void paradisuConnect() throws ClassNotFoundException, SQLException {
        dataSource = new MariaDbDataSource();
        
        FileConfiguration config = ParadisuMain.getEnvConfig();

        String jdbc = config.getString("Paradisu_HOST");
        int port = config.getInt("Paradisu_PORT");
        String username = config.getString("Paradisu_USERNAME");
        String password = config.getString("Paradisu_PASSWORD");

        dataSource.setServerName(jdbc);
        dataSource.setPort(port);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setDatabaseName("s21_Paradisu");

        

        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Paradisu] Now connected to the Database");
        
    }

    private static void testDataSource(DataSource dataSource) throws SQLException{
        try (Connection conn = dataSource.getConnection()){
            if(!conn.isValid(1)){
                throw new SQLException("Could not establish databse connection.");
            }

        }

    }
}
