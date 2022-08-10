package net.paradisu.paradisuplugin.bukkit.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class PlaytimeEvents implements Listener {
     

    private static DataSource dataSource;
    
    private static void establishConnection(){
        if(dataSource == null)
            dataSource = ParadisuMain.getDBCon();
    }
    
    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    Player player;
    UUID playerUUID = player.getUniqueId();
    Long joinTime;

    //Player[] joinArray = {playerUUID, jointime};

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        long currentTime = Instant.now().getEpochSecond();

        //joinArray.put(playerUUID, currentTime);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        establishConnection();
        UUID playerUUID = event.getPlayer().getUniqueId();

        //Player[] playerInfo = joinArray.get(playerUUID);
        //long joinTime = playerInfo[1];
        long currentTime = Instant.now().getEpochSecond();

        long timeDifference = currentTime - joinTime;
        
        long currentPlayTime = 0;
        try (Connection connection = dataSource.getConnection(); PreparedStatement playerQuery = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")) {
            playerQuery.setString(1, playerUUID.toString());
            ResultSet playerResult = playerQuery.executeQuery();
            playerResult.next();
            currentPlayTime = playerResult.getLong("playtime");
        
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        Long newPlayTime = currentPlayTime += timeDifference;

        // set playtime
        try (Connection connection = dataSource.getConnection(); 
            PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET playtime = ? WHERE UUID = ?")) {
        
            statement.setLong(1, newPlayTime);
            statement.setString(2, playerUUID.toString());
            statement.executeQuery();

            } catch (SQLException e) {
                e.printStackTrace();
        }


        
    }

    public Long updatePlayTime(Long timeProvided) {
        Long playtime = timeProvided;

        // You update jointime 
        // this is for the command so it updates when you do the command.


        return playtime; 
    }
    


}
