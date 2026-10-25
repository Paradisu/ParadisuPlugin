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

package net.paradisu.capture.paper;

import io.netty.channel.Channel;
import net.kyori.adventure.key.Key;
import net.paradisu.capture.control.ControlServer;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.CaptureLog;
import net.paradisu.capture.core.Leg;
import net.paradisu.capture.core.ProtocolHints;
import net.paradisu.capture.netty.NettyInjector;
import net.paradisu.capture.netty.PipelineLayout;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;

/**
 * Dev-only packet capture for Paper. Hooks Paper's {@code ChannelInitializeListenerHolder}, which lives in the server
 * jar rather than paper-api, so it is reached by reflection. If Paper moves it, fix {@link #register()}.
 */
public final class CapturePaper extends JavaPlugin {
    private static final Key LISTENER_KEY = Key.key("paradisu", "capture");

    private CaptureEngine engine;
    private ControlServer control;
    private Class<?> holder;

    @Override
    public void onEnable() {
        String socket = System.getProperty("paradisu.capture.socket");
        if (socket == null || socket.isEmpty()) {
            getLogger().info("paradisu.capture.socket is not set; packet capture is inactive");
            return;
        }
        CaptureLog log = new CaptureLog() {
            @Override
            public void info(String message) {
                getLogger().info(message);
            }

            @Override
            public void warn(String message) {
                getLogger().warning(message);
            }
        };
        engine = new CaptureEngine(
                "paper", "1", ProtocolHints.load(System.getProperty("paradisu.capture.protocol")), log);
        try {
            control = new ControlServer(engine, Path.of(socket));
            control.start();
            register();
            getLogger().info("Packet capture ready (control socket " + socket + ")");
        } catch (Exception e) {
            getLogger().severe("Packet capture could not start: " + e);
            shutdown();
        }
    }

    private void register() throws ReflectiveOperationException {
        holder = Class.forName("io.papermc.paper.network.ChannelInitializeListenerHolder");
        Class<?> listenerType = Class.forName("io.papermc.paper.network.ChannelInitializeListener");
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return switch (method.getName()) {
                    case "afterInitChannel" -> {
                        NettyInjector.inject((Channel) args[0], Leg.FRONTEND, PipelineLayout.PAPER, engine);
                        yield null;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> "ParadisuCapture listener";
                };
            }
        };
        Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[] {listenerType}, handler);
        holder.getMethod("addListener", Key.class, listenerType).invoke(null, LISTENER_KEY, listener);
    }

    @Override
    public void onDisable() {
        shutdown();
    }

    private void shutdown() {
        if (holder != null) {
            try {
                holder.getMethod("removeListener", Key.class).invoke(null, LISTENER_KEY);
            } catch (ReflectiveOperationException ignored) {
                // server is stopping anyway
            }
        }
        if (engine != null) {
            engine.shutdown();
        }
        if (control != null) {
            control.close();
        }
    }
}
