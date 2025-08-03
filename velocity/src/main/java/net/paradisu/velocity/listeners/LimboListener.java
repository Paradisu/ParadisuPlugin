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
