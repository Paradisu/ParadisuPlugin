package net.paradisu.velocity.commands.util.teleport;

import com.velocitypowered.api.proxy.Player;
import de.themoep.connectorplugin.LocationInfo;

import java.util.HashMap;

public class TeleportHistory {
    private static HashMap<Player, LocationInfo> teleportHistory = new HashMap<Player, LocationInfo>();

    /**
     * Adds a player's location to the teleport history
     * @param player The player to be added
     * @param location The respective location of the player
     */
    public void addTeleport(Player player, LocationInfo location) {
        teleportHistory.put(player, location);
    }
    
    /**
     * Gets the location of a player from the teleport history
     * @param player The player to be retrieved
     * @return The respective location of the player
     */
    public LocationInfo getTeleport(Player player) {
        return teleportHistory.get(player);
    }
    
    /**
     * Removes a player's location from the teleport history
     * @param player The player to be removed
     */
    public void removeTeleport(Player player) {
        teleportHistory.remove(player);
    }
}