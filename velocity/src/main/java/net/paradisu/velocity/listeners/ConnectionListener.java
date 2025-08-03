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

package net.paradisu.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.paradisu.database.models.playerdata.PlayerModel;
import net.paradisu.database.models.playerdata.PlayerProxySessionModel;
import net.paradisu.velocity.ParadisuVelocity;

import java.time.Instant;

@RequiredArgsConstructor
public class ConnectionListener {
    private final ParadisuVelocity paradisu;

    /**
     * Handles player connection events to track their initial server and session.
     *
     * @param event The PlayerChooseInitialServerEvent
     */
    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        final Instant calltime = Instant.now();

        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager(); ) {
            entityManager.getTransaction().begin();
            PlayerModel playerModel =
                    entityManager.find(PlayerModel.class, event.getPlayer().getUniqueId());

            if (playerModel == null) {
                playerModel = PlayerModel.builder()
                        .uuid(event.getPlayer().getUniqueId())
                        .firstJoined(calltime)
                        .lastJoined(calltime)
                        .online(true)
                        .proxySessions(new java.util.HashSet<>())
                        .build();
                entityManager.persist(playerModel);
            } else {
                playerModel.lastJoined(calltime);
                playerModel.online(true);
            }

            PlayerProxySessionModel sessionModel = PlayerProxySessionModel.builder()
                    .player(playerModel)
                    .joinedAt(calltime)
                    .build();

            playerModel.proxySessions().add(sessionModel);
            entityManager.getTransaction().commit();

            event.getInitialServer().ifPresent(server -> {
                event.getPlayer().getVirtualHost().ifPresent(host -> {
                    if (paradisu.server().getConfiguration().getForcedHosts().get(host.getHostName()) != null) {
                        return;
                    }
                });
            });

            if (playerModel.lastServer() == null) {
                return;
            }

            paradisu.server().getServer(playerModel.lastServer()).ifPresent(event::setInitialServer);
        }
    }

    /**
     * Updates the player's last server when they connect to a new server.
     *
     * @param event The ServerConnectedEvent
     */
    @Subscribe(order = PostOrder.EARLY)
    public void onServerConnected(ServerConnectedEvent event) {

        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager(); ) {
            entityManager.getTransaction().begin();
            PlayerModel playerModel =
                    entityManager.find(PlayerModel.class, event.getPlayer().getUniqueId());

            if (playerModel == null) {
                paradisu.logger()
                        .warn("Player model not found on server connect for UUID: "
                                + event.getPlayer().getUniqueId());
                return;
            }

            playerModel.lastServer(event.getServer().getServerInfo().getName());
            entityManager.getTransaction().commit();
        }
    }

    /**
     * Handles player disconnection to update their last seen time and playtime.
     *
     * @param event The DisconnectEvent
     */
    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        final Instant calltime = Instant.now();

        try (EntityManager entityManager = paradisu.databaseSession().factory().createEntityManager(); ) {
            entityManager.getTransaction().begin();
            PlayerModel playerModel =
                    entityManager.find(PlayerModel.class, event.getPlayer().getUniqueId());

            if (playerModel == null) {
                paradisu.logger()
                        .warn("Player model not found on proxy disconnect for UUID: "
                                + event.getPlayer().getUniqueId());
                return;
            }

            playerModel.lastSeen(calltime);

            entityManager
                    .createQuery(
                            "UPDATE PlayerProxySessionModel p SET p.leftAt = :leftAt WHERE p.player.uuid = :uuid AND p.leftAt IS NULL")
                    .setParameter("leftAt", calltime)
                    .setParameter("uuid", event.getPlayer().getUniqueId())
                    .executeUpdate();

            playerModel.playtime(playerModel.playtime()
                    + (calltime.toEpochMilli() - playerModel.lastJoined().toEpochMilli()));
            entityManager.getTransaction().commit();
        }
    }
}
