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

package net.paradisu.paper.messaging;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.connector.MessageTarget;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.paradisu.core.messaging.MessagingManager;
import net.paradisu.paper.ParadisuPaper;
import net.paradisu.paper.messaging.ServerState.PlayerState;
import net.paradisu.paper.messaging.messages.ServerHeartbeatMessage;
import net.paradisu.paper.messaging.messages.SyncWarpsMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Accessors(fluent = true)
public class PaperMessagingManager extends MessagingManager<BukkitConnectorPlugin, Player> {
    /** Ticks between heartbeats. */
    public static final long HEARTBEAT_INTERVAL_TICKS = 100L;
    /** Milliseconds without a heartbeat after which a server is considered offline. */
    public static final long SERVER_TIMEOUT_MILLIS = 15_000L;

    /** Last known state of every other server in the cluster, keyed by ConnectorPlugin server name. */
    @Getter
    private final Map<String, ServerState> clusterState = new ConcurrentHashMap<>();

    private final ParadisuPaper paradisu;

    public PaperMessagingManager(ParadisuPaper paradisu) {
        super(paradisu);
        this.paradisu = paradisu;
    }

    @Override
    public void registerMessages() {
        this.register(new SyncWarpsMessage(this.paradisu));
        this.register(new ServerHeartbeatMessage(this.paradisu));
    }

    /** Starts broadcasting this server's state to the cluster on the main thread. */
    public void startHeartbeat() {
        Bukkit.getScheduler()
                .runTaskTimer(this.paradisu, () -> this.broadcastHeartbeat(false), 20L, HEARTBEAT_INTERVAL_TICKS);
    }

    /**
     * Broadcasts this server's warp context and online players to the other servers. Must be called on the main thread.
     *
     * <p>Heartbeats are only delivered to servers with players online, since empty servers do not need cluster state
     * and a queued heartbeat would be stale on delivery. With the plugin message connector an empty server cannot send
     * either, so it is not advertised until a player joins.
     *
     * @param closing whether this server is shutting down
     */
    public void broadcastHeartbeat(boolean closing) {
        this.pruneStaleServers();
        if (this.paradisu.connector().getConnector().requiresPlayer()
                && Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        List<PlayerState> players = closing
                ? List.of()
                : Bukkit.getOnlinePlayers().stream()
                        .map(p -> new PlayerState(p.getUniqueId(), p.getName()))
                        .toList();
        this.send(
                MessageTarget.OTHERS_WITH_PLAYERS,
                new ServerHeartbeatMessage.Payload(
                        this.paradisu.paradisuConfig().context().warp(), closing, players));
    }

    /**
     * Records the state reported by another server's heartbeat.
     *
     * @param serverName the ConnectorPlugin name of the server
     * @param context the server's warp context
     * @param players the players online on the server
     */
    public void updateServerState(String serverName, String context, List<PlayerState> players) {
        this.clusterState.put(
                serverName, new ServerState(serverName, context, List.copyOf(players), System.currentTimeMillis()));
    }

    /**
     * Forgets a server, e.g. after it announced it is shutting down.
     *
     * @param serverName the ConnectorPlugin name of the server
     */
    public void removeServerState(String serverName) {
        this.clusterState.remove(serverName);
    }

    /** Drops servers that have not sent a heartbeat within {@link #SERVER_TIMEOUT_MILLIS}. */
    public void pruneStaleServers() {
        long now = System.currentTimeMillis();
        this.clusterState.values().removeIf(s -> now - s.lastHeartbeat() >= SERVER_TIMEOUT_MILLIS);
    }

    /**
     * Returns the other servers that sent a heartbeat recently.
     *
     * @return the live remote servers
     */
    public Stream<ServerState> liveServers() {
        long now = System.currentTimeMillis();
        return this.clusterState.values().stream().filter(s -> now - s.lastHeartbeat() < SERVER_TIMEOUT_MILLIS);
    }

    /**
     * Returns the server a player should be sent to for a warp context. The preferred server is used if it serves the
     * context; otherwise the least populated live server (including this one) is chosen.
     *
     * @param context the warp context
     * @param preferredServer the server to use if it serves the context, usually the player's current server
     * @return the server name, or empty if no live server serves the context
     */
    public Optional<String> findServerForContext(String context, String preferredServer) {
        String localServer = this.paradisu.connector().getServerName();
        List<ServerState> candidates = new ArrayList<>(
                this.liveServers().filter(s -> s.warpContext().equals(context)).toList());
        if (this.paradisu.paradisuConfig().context().warp().equals(context)) {
            candidates.add(new ServerState(
                    localServer,
                    context,
                    Bukkit.getOnlinePlayers().stream()
                            .map(p -> new PlayerState(p.getUniqueId(), p.getName()))
                            .toList(),
                    System.currentTimeMillis()));
        }

        return candidates.stream()
                .filter(s -> s.serverName().equals(preferredServer))
                .findFirst()
                .or(() -> candidates.stream()
                        .min(Comparator.comparingInt(s -> s.onlinePlayers().size())))
                .map(ServerState::serverName);
    }

    /**
     * Finds an online player anywhere in the cluster by name, preferring players on this server.
     *
     * @param name the player name, case-insensitive
     * @return the player, or empty if no live server reports them
     */
    public Optional<ClusterPlayer> findPlayer(String name) {
        Player localPlayer = Bukkit.getPlayerExact(name);
        if (localPlayer != null) {
            return Optional.of(new ClusterPlayer(
                    localPlayer.getUniqueId(),
                    localPlayer.getName(),
                    this.paradisu.connector().getServerName(),
                    true));
        }

        return this.liveServers()
                .flatMap(state -> state.onlinePlayers().stream()
                        .filter(p -> p.name().equalsIgnoreCase(name))
                        .map(p -> new ClusterPlayer(p.uuid(), p.name(), state.serverName(), false)))
                .findFirst();
    }

    /**
     * Returns the names of all players online across the cluster.
     *
     * @return the player names
     */
    public List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
        this.liveServers().forEach(s -> s.onlinePlayers().forEach(p -> names.add(p.name())));
        return names;
    }
}
