package net.paradisu.velocity.pack;

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
