package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import me.jakedadream.ParadisuPlugin.paradisu_mysql.DBConnections;
import net.md_5.bungee.api.ChatColor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerData {

    private static Connection connection;
    
    private static void establishConnection(){
        if(connection == null)  connection = DBConnections.ParadisuSQLCon();
    }

    public static void createPlayer(Player player) {  // https://www.youtube.com/watch?v=BIww-sYzEdU
        establishConnection();
        try {

            player.getFirstPlayed();

            UUID player_uuid = player.getUniqueId();
            String player_name = player.getName();
            
            if (!exists(player_uuid)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Creating new player data for " + player_name);
                PreparedStatement new_player_ps = connection.prepareStatement("INSERT IGNORE INTO PlayerData (UUID, NAME) VALUES (?,?)");
                new_player_ps.setString(1, player_uuid.toString());
                new_player_ps.setString(2, player_name);

                new_player_ps.executeUpdate();
                
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePlayer(Player player) {
        establishConnection();
        //write code to check all columns of the player data table and update them if they are different/null
        try{
            PreparedStatement playerQuery = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?");
            playerQuery.setString(1, player.getUniqueId().toString());

            ResultSet playerResult = playerQuery.executeQuery();
            playerResult.next();
            //loop through playerRestult to see if any are null
            if(playerResult.getString("first_played") == null){
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Updating first_played player data for " + player.getName());
                PreparedStatement updatePlayer = connection.prepareStatement("UPDATE PlayerData SET first_played = ? WHERE UUID = ?");
                updatePlayer.setLong(1, player.getFirstPlayed());
                updatePlayer.setString(2, player.getUniqueId().toString());
                updatePlayer.executeUpdate();
            }
    

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean exists (UUID uuid) {
        try {
            PreparedStatement prepared_statement = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID=?");
            prepared_statement.setString(1, uuid.toString());

            ResultSet results = prepared_statement.executeQuery();
            if (results.next()) {
                // player is found
                //send message to console that player is found


                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "exists(): player found: " + results.getString(1));
                return true;
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "exists(): player not found for UUID: " + uuid);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
