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

import de.themoep.connectorplugin.bukkit.Bridge;
import de.themoep.connectorplugin.connector.MessageTarget;
import net.paradisu.core.messaging.MessagingManager;
import net.paradisu.paper.ParadisuPaper;

public class PaperMessagingManager extends Bridge implements MessagingManager {
    private final ParadisuPaper paradisu;

    public PaperMessagingManager(ParadisuPaper paradisu) {
        super(paradisu.connector());
        this.paradisu = paradisu;
    }

    @Override
    public void registerMessages() {
        this.registerMessageHandler(MessageType.SYNC_WARPS, (_receiver, _message) -> {
            paradisu.warpManager().syncWarps();
        });
    }

    public void sendSyncWarps() {
        this.sendData(MessageType.SYNC_WARPS, MessageTarget.ALL_QUEUE, null);
    }
}
