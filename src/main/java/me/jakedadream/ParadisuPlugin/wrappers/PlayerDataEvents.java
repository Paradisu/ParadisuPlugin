package me.jakedadream.ParadisuPlugin.wrappers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.jakedadream.ParadisuPlugin.events.snwevents;
import me.jakedadream.ParadisuPlugin.paradisu_mysql.DBConnections;
import me.jakedadream.ParadisuPlugin.paradisu_mysql.PlayerData;
import me.jakedadream.ParadisuPlugin.wrappers.PlayerDataGetter;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDataEvents implements Listener {

    private static Connection connection;
    
    private static void establishConnection(){
        if(connection == null)  connection = DBConnections.ParadisuSQLCon();
    }


    @EventHandler
    public static void PlayerCreateEvent(PlayerJoinEvent event) {
    
        Player player = event.getPlayer();

    
        PlayerData.createPlayer(player);
        PlayerData.updatePlayer(player);
    }

    @EventHandler
    public static void PlayerJoinCounter(PlayerJoinEvent event) {
        establishConnection();
        Player player = event.getPlayer();

        try {
            String playeruuid = player.getUniqueId().toString();

            PreparedStatement PlayerJoinsQuery = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID =?");
            PlayerJoinsQuery.setString(1, playeruuid);
            ResultSet PlayerJoinsQueryResult = PlayerJoinsQuery.executeQuery();
            PlayerJoinsQueryResult.next();

            int number_of_joins = PlayerJoinsQueryResult.getInt("amount_of_joins");
            int number_of_joins_before = number_of_joins;
            number_of_joins++;

            PreparedStatement UpdatePlayerJoins = connection.prepareStatement("UPDATE PlayerData SET amount_of_joins =? WHERE UUID =?");
            UpdatePlayerJoins.setInt(1, number_of_joins);
            UpdatePlayerJoins.setString(2, playeruuid);
            UpdatePlayerJoins.executeUpdate();

            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Changed a player's amount of joins from " + number_of_joins_before + " to " + number_of_joins);

            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        


    }
}
