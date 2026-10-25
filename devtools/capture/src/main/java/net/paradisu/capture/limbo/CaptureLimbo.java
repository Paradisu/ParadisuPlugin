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

package net.paradisu.capture.limbo;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.EventHandler;
import com.loohp.limbo.events.EventPriority;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.events.connection.ConnectionEstablishedEvent;
import com.loohp.limbo.network.Channel;
import com.loohp.limbo.network.ClientConnection;
import com.loohp.limbo.plugins.LimboPlugin;
import net.paradisu.capture.control.ControlServer;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.CaptureLog;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.Direction;
import net.paradisu.capture.core.Leg;
import net.paradisu.capture.core.ProtocolHints;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

/**
 * Dev-only packet capture for Limbo. Limbo has no netty, so each connection's streams are wrapped (before Limbo reads
 * anything) with taps that split the byte stream into frames. Limbo has no compression or encryption, so frames are
 * already plain. If Limbo renames {@code Channel.input/output} or {@code ClientConnection.setChannel}, fix
 * {@link #onConnect}.
 */
public final class CaptureLimbo extends LimboPlugin implements Listener {
    private CaptureEngine engine;
    private ControlServer control;
    private Field inputField;
    private Field outputField;
    private Method setChannel;

    @Override
    public void onEnable() {
        String socket = System.getProperty("paradisu.capture.socket");
        if (socket == null || socket.isEmpty()) {
            System.out.println("[ParadisuCapture] paradisu.capture.socket is not set; packet capture is inactive");
            return;
        }
        CaptureLog log = new CaptureLog() {
            @Override
            public void info(String message) {
                System.out.println("[ParadisuCapture] " + message);
            }

            @Override
            public void warn(String message) {
                System.err.println("[ParadisuCapture] WARNING: " + message);
            }
        };
        engine = new CaptureEngine(
                "limbo", "1", ProtocolHints.load(System.getProperty("paradisu.capture.protocol")), log);
        try {
            inputField = Channel.class.getDeclaredField("input");
            outputField = Channel.class.getDeclaredField("output");
            inputField.setAccessible(true);
            outputField.setAccessible(true);
            setChannel = ClientConnection.class.getDeclaredMethod(
                    "setChannel", DataInputStream.class, DataOutputStream.class);
            setChannel.setAccessible(true);
            control = new ControlServer(engine, Path.of(socket));
            control.start();
            Limbo.getInstance().getEventsManager().registerEvents(this, this);
            log.info("Packet capture ready (control socket " + socket + ")");
        } catch (Exception e) {
            log.warn("Packet capture could not start: " + e);
            onDisable();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConnect(ConnectionEstablishedEvent event) {
        try {
            ClientConnection connection = event.getConnection();
            Socket socket = connection.getSocket();
            Channel channel = connection.getChannel();
            InputStream in = (InputStream) inputField.get(channel);
            OutputStream out = (OutputStream) outputField.get(channel);
            String remote = socket.getRemoteSocketAddress() instanceof InetSocketAddress inet
                    ? inet.getHostString() + ":" + inet.getPort()
                    : String.valueOf(socket.getRemoteSocketAddress());
            ConnectionInfo info = engine.newConnection(Leg.FRONTEND, remote);
            info.pipelineNames = () -> List.of("paradisu-stream-tap");
            FrameSplitter inbound = new FrameSplitter(engine, info, Direction.SERVERBOUND);
            FrameSplitter outbound = new FrameSplitter(engine, info, Direction.CLIENTBOUND);
            // Replaces the channel's streams before Limbo reads the first byte; the old Channel shares the socket
            setChannel.invoke(
                    connection,
                    new DataInputStream(new TapInputStream(in, inbound, () -> engine.onClose(info))),
                    new DataOutputStream(new TapOutputStream(out, outbound)));
        } catch (Throwable t) {
            engine.log().warn("could not tap a Limbo connection: " + t);
        }
    }

    @Override
    public void onDisable() {
        if (engine != null) {
            engine.shutdown();
        }
        if (control != null) {
            control.close();
        }
    }
}
