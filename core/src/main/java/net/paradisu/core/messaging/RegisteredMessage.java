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

package net.paradisu.core.messaging;

import de.themoep.connectorplugin.ConnectorPlugin;

public abstract class RegisteredMessage<P extends ConnectorPlugin<R>, R> {
    private final String action;

    public RegisteredMessage(String action) {
        this.action = action;
    }

    public abstract void handleMessage(R player, byte[] message);

    public String action() {
        return this.action;
    }

    public interface SerialMessage {
        public String action();

        public default byte[] serialize() {
            return null;
        }
        ;

        public default SerialMessage deserialize(byte[] data) {
            return null;
        }
        ;
    }
}
