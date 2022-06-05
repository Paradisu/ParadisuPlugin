package net.paradisu.paradisuplugin.bukkit.playerdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.paradisu.paradisuplugin.bukkit.ParadisuMain;

public class PlayerDataEvents implements Listener {

    @EventHandler
    public static void PlayerCreateEvent(PlayerJoinEvent event) {
    
        Player player = event.getPlayer();

    
        PlayerData.createPlayer(player);
        PlayerData.updatePlayer(player);
    }

    @EventHandler
    public static void PlayerJoinCounter(PlayerJoinEvent event) {
       
        Player player = event.getPlayer();

        try (Connection conn = ParadisuMain.getDBCon().getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE PlayerData SET amount_of_joins = amount_of_joins + 1 WHERE UUID = ?")) {
            String playeruuid = player.getUniqueId().toString();

            ps.setString(1, playeruuid);
            ps.executeQuery();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
