/*
 * The official plugin for the Paradisu server. Copyright (C) 2025 Paradisu. https://paradisu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.paradisu.velocity.commands.util.teleport;

import com.velocitypowered.api.proxy.Player;
import de.themoep.connectorplugin.LocationInfo;

import java.util.HashMap;

public class TeleportHistory {
    private static HashMap<Player, LocationInfo> teleportHistory = new HashMap<Player, LocationInfo>();

    /**
     * Adds a player's location to the teleport history
     *
     * @param player The player to be added
     * @param location The respective location of the player
     */
    public void addTeleport(Player player, LocationInfo location) {
        teleportHistory.put(player, location);
    }

    /**
     * Gets the location of a player from the teleport history
     *
     * @param player The player to be retrieved
     * @return The respective location of the player
     */
    public LocationInfo getTeleport(Player player) {
        return teleportHistory.get(player);
    }

    /**
     * Removes a player's location from the teleport history
     *
     * @param player The player to be removed
     */
    public void removeTeleport(Player player) {
        teleportHistory.remove(player);
    }
}
