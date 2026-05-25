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

package net.paradisu.paper.messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.connectorplugin.LocationInfo;
import de.themoep.connectorplugin.bukkit.Bridge;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.connector.MessageTarget;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.paradisu.core.messaging.MessagingManager;
import net.paradisu.core.messaging.RegisteredMessage;
import net.paradisu.database.models.WarpModel;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ServerState.PlayerState;
import net.paradisu.paper.messaging.messages.ServerHeartbeatMessage;
import net.paradisu.paper.messaging.messages.SyncWarpsMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Accessors(fluent = true)
public class PaperMessagingManager extends Bridge implements MessagingManager<BukkitConnectorPlugin, Player> {
    @Getter
    private final Map<String, ServerState> clusterState = new ConcurrentHashMap<>();

    @Getter
    private final Set<ClusterPlayer> clusterPlayers = ConcurrentHashMap.newKeySet();

    private final ParadisuPaper paradisu;

    public PaperMessagingManager(ParadisuPaper paradisu) {
        super(paradisu.connector());
        this.paradisu = paradisu;
    }

    @Override
    public void register(RegisteredMessage<BukkitConnectorPlugin, Player> message) {
        this.registerHandler(message.action(), message::handleMessage);
    }

    @Override
    public void registerMessages() {
        this.register(new SyncWarpsMessage(paradisu));
        this.register(new ServerHeartbeatMessage(paradisu));
    }

    public void startHeartbeat() {
        Bukkit.getScheduler()
                .runTaskTimerAsynchronously(paradisu, () -> broadcastHeartbeat(false), 20L, 100L); // Every 5s
    }

    public void broadcastHeartbeat(boolean closing) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(paradisu.connector().getServerName());
        out.writeUTF(paradisu.paradisuConfig().context().warp());
        out.writeBoolean(closing);

        if (!closing) {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            out.writeInt(players.size());
            for (Player p : players) {
                out.writeLong(p.getUniqueId().getMostSignificantBits());
                out.writeLong(p.getUniqueId().getLeastSignificantBits());
                out.writeUTF(p.getName());
            }
        }
        sendData(ServerHeartbeatMessage.ACTION, MessageTarget.OTHERS_QUEUE, out.toByteArray());
    }

    public void updateServerState(String name, String context, List<PlayerState> players) {
        this.clusterState.put(name, new ServerState(name, context, players, System.currentTimeMillis()));
    }

    public void removeServerState(String name) {
        this.clusterState.remove(name);
    }

    public Optional<ServerState> findBestServerForContext(String context) {
        return this.clusterState.values().stream()
                .filter(s -> s.warpContext().equals(context))
                .filter(s -> System.currentTimeMillis() - s.lastHeartbeat() < 15000) // 15s timeout
                .min(Comparator.comparingInt(s -> s.onlinePlayers().size()));
    }

    /** Returns a list of all player names currently online across the entire cluster. */
    public List<String> getAllOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
        long now = System.currentTimeMillis();
        this.clusterState.values().stream()
                .filter(s -> now - s.lastHeartbeat() < 15000)
                .forEach(s -> s.onlinePlayers().forEach(p -> names.add(p.name())));
        return names;
    }

    /** Resolves the best server for the warp and instructs the remote proxy/server to move the specified player. */
    @SuppressWarnings("unchecked")
    public void teleportRemotePlayer(ClusterPlayer target, WarpModel warp) {
        Optional<ServerState> targetServer = findBestServerForContext(warp.context());

        if (targetServer.isPresent()) {
            LocationInfo locInfo = new LocationInfo(
                    targetServer.get().serverName(),
                    warp.world(),
                    warp.x(),
                    warp.y(),
                    warp.z(),
                    warp.yaw(),
                    warp.pitch());

            this.teleport(target.name(), locInfo, result -> {
                paradisu.logger().info("Remote teleport result for " + target.name() + ": " + result);
            });
        }
    }
}
