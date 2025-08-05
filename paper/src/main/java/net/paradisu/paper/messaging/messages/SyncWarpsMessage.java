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

package net.paradisu.paper.messaging.messages;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import net.paradisu.core.messaging.RegisteredMessage;
import net.paradisu.paper.ParadisuPaper;
import org.bukkit.entity.Player;

public class SyncWarpsMessage extends RegisteredMessage<BukkitConnectorPlugin, Player> {
    public static final String ACTION = "SYNC_WARPS";

    private ParadisuPaper paradisu;

    public SyncWarpsMessage(ParadisuPaper paradisu) {
        super(ACTION);
        this.paradisu = paradisu;
    }

    @Override
    public void handleMessage(Player player, byte[] message) {
        this.paradisu.warpManager().syncWarps();
    }

    public static class Message implements SerialMessage {
        @Override
        public String action() {
            return ACTION;
        }
    }
}
