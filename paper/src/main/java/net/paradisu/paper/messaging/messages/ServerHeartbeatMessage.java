/*
 * The official plugin for the Paradisu server. Copyright (C) 2026 Paradisu. https://paradisu.net
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

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import net.paradisu.core.messaging.RegisteredMessage;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ServerState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerHeartbeatMessage extends RegisteredMessage<BukkitConnectorPlugin, Player> {
    public static final String ACTION = "SERVER_HEARTBEAT";
    private final ParadisuPaper paradisu;

    public ServerHeartbeatMessage(ParadisuPaper paradisu) {
        super(ACTION);
        this.paradisu = paradisu;
    }

    @Override
    public void handleMessage(Player player, byte[] message) {
        @SuppressWarnings("null")
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String serverName = in.readUTF();
        String context = in.readUTF();
        boolean isClosing = in.readBoolean();

        if (isClosing) {
            paradisu.messagingManager().removeServerState(serverName);
            return;
        }

        int playerCount = in.readInt();
        List<ServerState.PlayerState> players = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            players.add(new ServerState.PlayerState(new UUID(in.readLong(), in.readLong()), in.readUTF()));
        }

        paradisu.messagingManager().updateServerState(serverName, context, players);
    }
}
