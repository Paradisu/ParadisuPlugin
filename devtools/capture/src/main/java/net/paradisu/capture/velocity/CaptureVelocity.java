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

package net.paradisu.capture.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import net.paradisu.capture.control.ControlServer;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.CaptureLog;
import net.paradisu.capture.core.Leg;
import net.paradisu.capture.core.ProtocolHints;
import net.paradisu.capture.netty.NettyInjector;
import net.paradisu.capture.netty.PipelineLayout;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Dev-only packet capture for Velocity. Wraps the proxy's frontend and backend channel initializers (via the internal
 * {@code ConnectionManager}, reached by reflection). If Velocity changes that, fix {@link #hook}.
 */
@Plugin(id = "paradisucapture", name = "ParadisuCapture", version = "1")
public final class CaptureVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private CaptureEngine engine;
    private ControlServer control;
    private Object connectionManager;
    private ChannelInitializer<Channel> originalFrontend;
    private ChannelInitializer<Channel> originalBackend;

    @Inject
    public CaptureVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        String socket = System.getProperty("paradisu.capture.socket");
        if (socket == null || socket.isEmpty()) {
            logger.info("paradisu.capture.socket is not set; packet capture is inactive");
            return;
        }
        CaptureLog log = new CaptureLog() {
            @Override
            public void info(String message) {
                logger.info(message);
            }

            @Override
            public void warn(String message) {
                logger.warn(message);
            }
        };
        engine = new CaptureEngine(
                "velocity", "1", ProtocolHints.load(System.getProperty("paradisu.capture.protocol")), log);
        try {
            control = new ControlServer(engine, Path.of(socket));
            control.start();
            hook();
            logger.info("Packet capture ready (control socket {})", socket);
        } catch (Exception e) {
            logger.error("Packet capture could not start", e);
            unhook();
        }
    }

    @SuppressWarnings("unchecked")
    private void hook() throws ReflectiveOperationException {
        Field field = server.getClass().getDeclaredField("cm");
        field.setAccessible(true);
        connectionManager = field.get(server);
        Class<?> type = connectionManager.getClass();
        Object frontendHolder = type.getMethod("getServerChannelInitializer").invoke(connectionManager);
        Object backendHolder = type.getMethod("getBackendChannelInitializer").invoke(connectionManager);
        Method frontendGet = frontendHolder.getClass().getMethod("get");
        Method backendGet = backendHolder.getClass().getMethod("get");
        // Generic holders erase to Object, so the bridge methods are the ones taking ChannelInitializer
        Method frontendSet = frontendHolder.getClass().getMethod("set", ChannelInitializer.class);
        Method backendSet = backendHolder.getClass().getMethod("set", ChannelInitializer.class);
        originalFrontend = (ChannelInitializer<Channel>) frontendGet.invoke(frontendHolder);
        originalBackend = (ChannelInitializer<Channel>) backendGet.invoke(backendHolder);
        frontendSet.invoke(frontendHolder, new CaptureInitializer(originalFrontend, Leg.FRONTEND, engine));
        backendSet.invoke(backendHolder, new CaptureInitializer(originalBackend, Leg.BACKEND, engine));
    }

    private void unhook() {
        if (connectionManager == null || originalFrontend == null) {
            return;
        }
        try {
            Class<?> type = connectionManager.getClass();
            Object frontendHolder =
                    type.getMethod("getServerChannelInitializer").invoke(connectionManager);
            Object backendHolder =
                    type.getMethod("getBackendChannelInitializer").invoke(connectionManager);
            frontendHolder
                    .getClass()
                    .getMethod("set", ChannelInitializer.class)
                    .invoke(frontendHolder, originalFrontend);
            backendHolder.getClass().getMethod("set", ChannelInitializer.class).invoke(backendHolder, originalBackend);
        } catch (ReflectiveOperationException ignored) {
            // proxy is shutting down
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        unhook();
        if (engine != null) {
            engine.shutdown();
        }
        if (control != null) {
            control.close();
        }
    }

    /** Runs the original initializer, then adds the capture handlers next to the codecs it installed. */
    @ChannelHandler.Sharable
    private static final class CaptureInitializer extends ChannelInitializer<Channel> {
        private final ChannelInitializer<Channel> original;
        private final Leg leg;
        private final CaptureEngine engine;

        CaptureInitializer(ChannelInitializer<Channel> original, Leg leg, CaptureEngine engine) {
            this.original = original;
            this.leg = leg;
            this.engine = engine;
        }

        @Override
        protected void initChannel(Channel channel) {
            // The channel is already registered, so adding the original runs its initChannel synchronously
            channel.pipeline().addLast(original);
            NettyInjector.inject(channel, leg, PipelineLayout.VELOCITY, engine);
        }
    }
}
