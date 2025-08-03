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

public class TeleportRequestHeader {
    private Player requestor;
    private Player requestee;

    /**
     * Constructor for TeleportRequestHeader
     *
     * @param requestor The player who requesting the teleport
     * @param requestee The player who is recieving the telport request
     */
    public void setRequestHeader(Player requestor, Player requestee) {
        this.requestor = requestor;
        this.requestee = requestee;
    }

    /**
     * Gets the player who is requesting the teleport
     *
     * @return The player who is requesting the teleport
     */
    public Player getRequestor() {
        return requestor;
    }

    /**
     * Gets the player who is recieving the teleport request
     *
     * @return The player who is recieving the teleport request
     */
    public Player getRequestee() {
        return requestee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((requestee == null) ? 0 : requestee.getUniqueId().hashCode());
        result = prime * result
                + ((requestor == null) ? 0 : requestor.getUniqueId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TeleportRequestHeader other = (TeleportRequestHeader) obj;
        if (requestee == null) {
            if (other.requestee != null) return false;
        } else if (!requestee.getUniqueId().equals(other.requestee.getUniqueId())) return false;
        if (requestor == null) {
            if (other.requestor != null) return false;
        } else if (!requestor.getUniqueId().equals(other.requestor.getUniqueId())) return false;
        return true;
    }
}
