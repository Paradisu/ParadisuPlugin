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

package net.paradisu.velocity.messaging;

import com.velocitypowered.api.proxy.Player;
import de.themoep.connectorplugin.velocity.VelocityConnectorPlugin;
import net.paradisu.core.messaging.MessagingManager;
import net.paradisu.velocity.ParadisuVelocity;

public class VelocityMessagingManager extends MessagingManager<VelocityConnectorPlugin, Player> {
    public VelocityMessagingManager(ParadisuVelocity paradisu) {
        super(paradisu);
    }

    /** Registers the messages handled by the proxy. There are none yet; add {@code this.register(...)} calls here. */
    @Override
    public void registerMessages() {}
}
