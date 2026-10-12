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
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.connector.Message;
import net.paradisu.core.messaging.RegisteredMessage;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ServerState.PlayerState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Periodic broadcast of a server's warp context and online players, used to route warps and find players. */
public class ServerHeartbeatMessage extends RegisteredMessage<BukkitConnectorPlugin, Player> {
    public static final String ACTION = "SERVER_HEARTBEAT";

    private final ParadisuPaper paradisu;

    public ServerHeartbeatMessage(ParadisuPaper paradisu) {
        super(ACTION);
        this.paradisu = paradisu;
    }

    @Override
    public void handle(Player receiver, Message message) {
        Payload payload = Payload.deserialize(message.getData());
        String serverName = message.getSendingServer();

        if (payload.closing()) {
            this.paradisu.messagingManager().removeServerState(serverName);
        } else {
            this.paradisu.messagingManager().updateServerState(serverName, payload.warpContext(), payload.players());
        }
    }

    /**
     * Heartbeat payload. The sending server's name is taken from the message itself.
     *
     * @param warpContext the warp context of the sending server
     * @param closing whether the sending server is shutting down
     * @param players the players online on the sending server
     */
    public record Payload(String warpContext, boolean closing, List<PlayerState> players) implements SerialMessage {
        @Override
        public String action() {
            return ACTION;
        }

        @Override
        public byte[] serialize() {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(this.warpContext);
            out.writeBoolean(this.closing);
            out.writeInt(this.players.size());
            for (PlayerState player : this.players) {
                out.writeLong(player.uuid().getMostSignificantBits());
                out.writeLong(player.uuid().getLeastSignificantBits());
                out.writeUTF(player.name());
            }
            return out.toByteArray();
        }

        /**
         * Reads a payload written by {@link #serialize()}.
         *
         * @param data the serialized payload
         * @return the payload
         */
        public static Payload deserialize(byte[] data) {
            ByteArrayDataInput in = ByteStreams.newDataInput(data);
            String warpContext = in.readUTF();
            boolean closing = in.readBoolean();
            int playerCount = in.readInt();
            List<PlayerState> players = new ArrayList<>(playerCount);
            for (int i = 0; i < playerCount; i++) {
                players.add(new PlayerState(new UUID(in.readLong(), in.readLong()), in.readUTF()));
            }
            return new Payload(warpContext, closing, players);
        }
    }
}
