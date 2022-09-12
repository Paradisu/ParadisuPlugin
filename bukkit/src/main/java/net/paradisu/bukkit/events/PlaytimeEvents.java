package net.paradisu.bukkit.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.paradisu.bukkit.ParadisuMain;

public class PlaytimeEvents implements Listener {
     

    private static DataSource dataSource;
    
    private static void establishConnection(){
        if(dataSource == null)
            dataSource = ParadisuMain.getDBCon();
    }
    
    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    //
    static Map<UUID, Long> uuidJoinMap = new HashMap<UUID, Long>();
    //


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        long currentTime = Instant.now().getEpochSecond();

        uuidJoinMap.put(playerUUID, currentTime);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        updatePlayTime(playerUUID);
    }

    public static void updatePlayTime(UUID playerUUID) {
        establishConnection();
        long playerJoinTime = uuidJoinMap.get(playerUUID);
        long currentTime = Instant.now().getEpochSecond();

        long sessionDifference = currentTime - playerJoinTime;
        
        long currentPlayTime = 0;
        try (Connection connection = dataSource.getConnection(); PreparedStatement playerQuery = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")) {
            playerQuery.setString(1, playerUUID.toString());
            ResultSet playerResult = playerQuery.executeQuery();
            playerResult.next();
            currentPlayTime = playerResult.getLong("playtime");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        Long newPlayTime = currentPlayTime += sessionDifference;

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
    


}
