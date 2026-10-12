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
import de.themoep.connectorplugin.connector.ConnectingPlugin;
import de.themoep.connectorplugin.connector.MessageTarget;
import net.paradisu.core.ParadisuPlugin;
import net.paradisu.core.utils.Constants;

/**
 * Sends and receives Paradisu messages between servers and proxies through ConnectorPlugin.
 *
 * <p>Messages are sent and handled under this plugin's own name, so they never collide with ConnectorPlugin's built-in
 * bridge actions. Both the Paper and Velocity plugins use the same name, so a message sent by one platform is handled
 * by any registered handler with the same action on the other.
 *
 * @param <P> the platform ConnectorPlugin type
 * @param <R> the platform player type
 */
public abstract class MessagingManager<P extends ConnectorPlugin<R>, R> implements ConnectingPlugin {
    protected final ParadisuPlugin<P, R> paradisu;

    public MessagingManager(ParadisuPlugin<P, R> paradisu) {
        this.paradisu = paradisu;
    }

    /**
     * Returns the name messages are sent and handled under.
     *
     * @return the plugin name
     */
    @Override
    public String getName() {
        return Constants.Plugin.NAME;
    }

    /** Registers every message handled on this platform; called once on enable. */
    public abstract void registerMessages();

    /**
     * Registers a message handler for its action. Exceptions thrown by the handler are caught and logged.
     *
     * @param message the message handler to register
     */
    public void register(RegisteredMessage<P, R> message) {
        this.paradisu.connector().getConnector().registerMessageHandler(this, message.action(), (receiver, data) -> {
            try {
                message.handle(receiver, data);
            } catch (Exception e) {
                this.paradisu
                        .logger()
                        .error("Failed to handle " + message.action() + " message from " + data.getSendingServer(), e);
            }
        });
    }

    /** Unregisters every message handler registered by this manager; called on disable. */
    public void unregisterAll() {
        this.paradisu.connector().getConnector().unregisterMessageHandlers(this);
    }

    /**
     * Sends a message to the given target.
     *
     * @param target where to send the message
     * @param message the message to send
     */
    public void send(MessageTarget target, RegisteredMessage.SerialMessage message) {
        this.sendData(message.action(), target, message.serialize());
    }

    /**
     * Sends a message to a specific server or proxy.
     *
     * @param target {@link MessageTarget#SERVER} or {@link MessageTarget#PROXY}
     * @param targetData the server name (prefixed with {@code server:}) or player name to route by
     * @param message the message to send
     */
    public void send(MessageTarget target, String targetData, RegisteredMessage.SerialMessage message) {
        this.sendData(message.action(), target, targetData, message.serialize());
    }

    /**
     * Sends raw data for an action to the given target.
     *
     * @param action the action of the message
     * @param target where to send the message
     * @param data the serialized payload
     */
    public void sendData(String action, MessageTarget target, byte[] data) {
        this.paradisu.connector().getConnector().sendData(this, action, target, data);
    }

    /**
     * Sends raw data for an action to a specific server or proxy.
     *
     * @param action the action of the message
     * @param target {@link MessageTarget#SERVER} or {@link MessageTarget#PROXY}
     * @param targetData the server name (prefixed with {@code server:}) or player name to route by
     * @param data the serialized payload
     */
    public void sendData(String action, MessageTarget target, String targetData, byte[] data) {
        this.paradisu.connector().getConnector().sendData(this, action, target, targetData, data);
    }
}
