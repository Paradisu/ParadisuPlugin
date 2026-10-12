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

package net.paradisu.core.messaging;

import de.themoep.connectorplugin.ConnectorPlugin;
import de.themoep.connectorplugin.connector.Message;

/**
 * Handler for one message action, registered with {@link MessagingManager#register(RegisteredMessage)}.
 *
 * @param <P> the platform ConnectorPlugin type
 * @param <R> the platform player type
 */
public abstract class RegisteredMessage<P extends ConnectorPlugin<R>, R> {
    private final String action;

    public RegisteredMessage(String action) {
        this.action = action;
    }

    /**
     * Handles a received message. On Paper this runs on the main thread, so do not block in it; on Velocity it runs on
     * a connector thread.
     *
     * @param receiver the player the message was received through, may be null
     * @param message the received message; use {@link Message#getData()} for the payload and
     *     {@link Message#getSendingServer()} for the sender
     */
    public abstract void handle(R receiver, Message message);

    /**
     * Returns the action this handler is registered for.
     *
     * @return the action
     */
    public String action() {
        return this.action;
    }

    /** Outgoing payload for a message action, sent with {@link MessagingManager#send}. */
    public interface SerialMessage {
        /**
         * Returns the action the payload is sent under.
         *
         * @return the action
         */
        public String action();

        /**
         * Serializes the payload. Messages without data use the default empty payload.
         *
         * @return the serialized payload, never null
         */
        public default byte[] serialize() {
            return new byte[0];
        }
    }
}
