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

package net.paradisu.capture.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.Direction;
import net.paradisu.capture.core.Leg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Adds the capture handlers to a freshly initialised channel. Shared by Paper and Velocity. */
public final class NettyInjector {
    static final String INBOUND = "paradisu_capture_in";
    static final String OUTBOUND = "paradisu_capture_out";

    private static volatile boolean warnedLayout;

    private NettyInjector() {}

    /** Injects capture handlers; failures are logged and leave the connection untouched. */
    public static void inject(Channel channel, Leg leg, PipelineLayout layout, CaptureEngine engine) {
        try {
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(INBOUND) != null) {
                return;
            }
            ConnectionInfo info = engine.newConnection(leg, CaptureInboundHandler.describe(channel.remoteAddress()));
            // Inbound frames on a frontend go to the server; on a backend link they come from it
            Direction inbound = leg == Leg.FRONTEND ? Direction.SERVERBOUND : Direction.CLIENTBOUND;
            if (!anchor(pipeline, engine, info, layout, inbound)) {
                engine.onClose(info);
                return;
            }
            info.pipelineNames = () -> {
                try {
                    return channel.eventLoop()
                            .submit(() -> new ArrayList<>(channel.pipeline().names()))
                            .get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    return List.of("(unavailable: " + e + ")");
                }
            };
        } catch (Throwable t) {
            engine.log().warn("could not inject the capture handlers: " + t);
        }
    }

    /**
     * (Re)places the capture handlers right before the codecs. Must run on the channel's event loop.
     *
     * @return false when no codec anchor was found
     */
    static boolean anchor(
            ChannelPipeline pipeline,
            CaptureEngine engine,
            ConnectionInfo info,
            PipelineLayout layout,
            Direction inbound) {
        String decoder = first(pipeline, layout.decoderAnchors());
        String encoder = first(pipeline, layout.encoderAnchors());
        if (decoder == null || encoder == null) {
            if (!warnedLayout) {
                warnedLayout = true;
                engine.log()
                        .warn("capture: no " + layout.name() + " codec handler found in " + pipeline.names()
                                + "; update PipelineLayout");
            }
            return false;
        }
        if (pipeline.get(INBOUND) != null) {
            pipeline.remove(INBOUND);
        }
        if (pipeline.get(OUTBOUND) != null) {
            pipeline.remove(OUTBOUND);
        }
        pipeline.addBefore(decoder, INBOUND, new CaptureInboundHandler(engine, info, layout, inbound));
        pipeline.addBefore(encoder, OUTBOUND, new CaptureOutboundHandler(engine, info, inbound.opposite()));
        return true;
    }

    private static String first(ChannelPipeline pipeline, List<String> names) {
        for (String name : names) {
            if (pipeline.get(name) != null) {
                return name;
            }
        }
        return null;
    }
}
