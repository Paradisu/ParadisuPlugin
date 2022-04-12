package me.jakedadream.ParadisuPlugin.databaseHandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jakedadream.ParadisuPlugin.ParadisuMain;
import me.jakedadream.ParadisuPlugin.wrappers.PlayerDataGetter;
import net.md_5.bungee.api.ChatColor;

public class PlayerData {

    private static DataSource dataSource;
    
    private static void establishConnection(){
        if(dataSource == null)
            dataSource = ParadisuMain.getDBCon();
    }

    public static void createPlayer(Player player) {
        
        

            player.getFirstPlayed();

            UUID player_uuid = player.getUniqueId();
            String player_name = player.getName();
            
            if (!exists(player_uuid)) {
                establishConnection();
                
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Creating new player data for " + player_name);
                
                try(Connection conn = dataSource.getConnection(); PreparedStatement new_player_ps = conn.prepareStatement("INSERT IGNORE INTO PlayerData (UUID, NAME) VALUES (?,?)")){
                    new_player_ps.setString(1, player_uuid.toString());
                    new_player_ps.setString(2, player_name);
    
                    new_player_ps.executeUpdate();
                } catch (SQLException e){
                    e.printStackTrace();
                }
                
                
            }




        
    }

    public static void updatePlayer(Player player) {
        establishConnection();
        PlayerDataGetter data = new PlayerDataGetter();
        //write code to check all columns of the player data table and update them if they are different/null
        try(Connection conn = dataSource.getConnection(); PreparedStatement playerQuery = conn.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")){
            
            playerQuery.setString(1, player.getUniqueId().toString());

            ResultSet playerResult = playerQuery.executeQuery();
            playerResult.next();
            if(playerResult.getString("first_played") == null){
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Updating first_played player data for " + player.getName());
                try(PreparedStatement updatePlayer = conn.prepareStatement("UPDATE PlayerData SET first_played = ? WHERE UUID = ?")){
                    updatePlayer.setLong(1, player.getFirstPlayed());
                    updatePlayer.setString(2, player.getUniqueId().toString());
                    updatePlayer.executeUpdate();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }

            if(playerResult.getString("top_rank") == null || !(playerResult.getString("top_rank").equals(data.GetPlayerTopRank(player)))){
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Updating top_rank player data for " + player.getName());
                try(PreparedStatement updatePlayer = conn.prepareStatement("UPDATE PlayerData SET top_rank = ? WHERE UUID = ?")){
                    updatePlayer.setString(1, data.GetPlayerTopRank(player));
                    updatePlayer.setString(2, player.getUniqueId().toString());
                    updatePlayer.executeUpdate();
                } catch(SQLException e){
                    e.printStackTrace();
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
       
    }

    public static boolean exists (UUID uuid) {
        establishConnection();
        try (Connection conn = dataSource.getConnection(); PreparedStatement prepared_statement = conn.prepareStatement("SELECT * FROM PlayerData WHERE UUID=?")){
            prepared_statement.setString(1, uuid.toString());

            ResultSet results = prepared_statement.executeQuery();
            if (results.next()) {
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
