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
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.RequiredArgsConstructor;
import net.paradisu.core.packs.PackManager;
import net.paradisu.velocity.ParadisuVelocity;

import java.util.Optional;

@RequiredArgsConstructor
public class PackListener {
    private final ParadisuVelocity paradisu;
    private final PackManager packManager;

    /**
     * Sends the default resource pack to the player when they connect
     *
     * @param event The ServerPostConnectEvent
     */
    @Subscribe(order = PostOrder.EARLY)
    public void onPostConnect(ServerPostConnectEvent event) {
        // TODO: Track per-player pack state
        // For now, we can just assume they will not have any packs applied if they just joined the proxy
        if (event.getPreviousServer() != null) return;

        final Player player = event.getPlayer();
        final Optional<ServerConnection> serverConnection = player.getCurrentServer();

        serverConnection.ifPresent(connection -> {
            if (this.packManager.defaultRequest().isDone()) {
                try {
                    player.sendResourcePacks(this.packManager.defaultRequest().get());
                } catch (Exception e) {
                    paradisu.logger().error("Failed to send default resource pack to player", e);
                }
            } else {
                paradisu.logger().error("Default resource pack request is not finished loading");
            }
        });
    }
}
