package net.paradisu.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ListenerBoundEvent;
import com.velocitypowered.api.network.ListenerType;
import lombok.RequiredArgsConstructor;
import net.paradisu.velocity.ParadisuVelocity;

@RequiredArgsConstructor
public class LimboListener {
    private final ParadisuVelocity paradisu;

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
