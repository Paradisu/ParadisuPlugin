package net.paradisu.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.network.ListenerType;
import lombok.RequiredArgsConstructor;
import net.paradisu.velocity.ParadisuVelocity;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public class LimboListener {
    private final ParadisuVelocity paradisu;
    
    /**
     * Transfers all players to the limbo server when the proxy shuts down.
     *
     * @param event The ProxyShutdownEvent
     */
    @Subscribe(order = PostOrder.LAST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        final InetSocketAddress limboAddress = new InetSocketAddress(
            paradisu.paradisuConfig().limboServer().host(),
            paradisu.paradisuConfig().limboServer().port()
        );
        paradisu.server().getAllPlayers().forEach(player -> {
            player.transferToHost(limboAddress);
        });
    }

    /**
     * Runs a command to transfer all players from the limbo server when the Minecraft listener is bound.
     *
     * @param event The ListenerBoundEvent
     */
    @SuppressWarnings("unchecked")
    @Subscribe(order = PostOrder.LAST)
    public void onListenerBoundEvent(ListenerBoundEvent event) {
        if (!event.getListenerType().equals(ListenerType.MINECRAFT)) {
            return;
        }

        StringBuilder command = new StringBuilder("transferall ")
            .append(paradisu.paradisuConfig().transferReturnTarget().host())
            .append(" ")
            .append(paradisu.paradisuConfig().transferReturnTarget().port());

        paradisu.connector().getBridge().runServerConsoleCommand("limbo", command.toString());
    }
}
