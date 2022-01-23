package me.jakedadream.ParadisuPlugin.paradisu_mysql;

import me.jakedadream.ParadisuPlugin.paradisu_mysql.DBConnections;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerData {

    DBConnections dbConnections = new DBConnections();
    Connection connection = dbConnections.ParadisuSQLCon();

    public void createPlayer(Player player) {  // https://www.youtube.com/watch?v=BIww-sYzEdU
        try {

            UUID player_uuid = player.getUniqueId();
            String player_name = player.getName();
            
            if (!exists(player_uuid)) {
                PreparedStatement new_player_ps = connection.prepareStatement("INSERT IGNORE INTO PlayerData (NAME,UUID) VALUES (?,?)");
                new_player_ps.setString(1, player_uuid.toString());
                new_player_ps.setString(2, player_name);

            }




        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists (UUID uuid) {
        try {
            PreparedStatement prepared_statement = connection.prepareStatement("SELECT * FROM PlayerData WHERE UUID=?");
            prepared_statement.setString(1, uuid.toString());

            ResultSet results = prepared_statement.executeQuery();
            if (results.next()) {
                // player is found
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
