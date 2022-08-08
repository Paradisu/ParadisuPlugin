package net.paradisu.paradisuplugin.bukkit.events;

import java.util.UUID;

public class PlaytimeEvents implements Listener {
    /* 

    private static DataSource dataSource;
    
    private static void establishConnection(){
        if(dataSource == null)
            dataSource = ParadisuMain.getDBCon();
    }
    
    String cmdprefix = ParadisuMain.CommandPrefix();
    String cmdemph = ParadisuMain.CommandEmph();

    Player player;
    Long joinTime;
    Player[] joinArray = {playerUUID, jointime};

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        long currentTime = Instant.now().getEpochSecond();

        joinArray.put(playerUUID, currentTime);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        establishConnection();
        UUID playerUUID = event.getPlayer().getUniqueId();

        Player[] playerInfo = joinArray.get(playerUUID);
        long joinTime = playerInfo[1];
        long currentTime = Instant.now().getEpochSecond();

        long timeDifference = currentTime - joinTime;
        
        long currentPlayTime;
        try (Connection connection = dataSource.getConnection(); PreparedStatement playerQuery = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")) {
            playerQuery.setString(1, playerUUID);
            ResultSet playerResult = playerQuery.executeQuery();
            playerResult.next();
            currentPlayTime = playerResult.getString("playtime");
        
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        Long newPlayTime = currentPlayTime += timeDifference;

        // set playtime
        try (Connection connection = dataSource.getConnection(); 
            PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET playtime = ? WHERE UUID = ?")) {
        
            statement.setString(1, newPlayTime);
            statement.setString(2, playerUUID);
            statement.executeQuery();

            } catch (SQLException e) {
                e.printStackTrace();
        }


        
    }

    */


}
