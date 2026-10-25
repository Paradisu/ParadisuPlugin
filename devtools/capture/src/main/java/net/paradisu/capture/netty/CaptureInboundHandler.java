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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.Direction;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/** Taps inbound frames, forwards everything unchanged and never lets a capture failure reach the connection. */
final class CaptureInboundHandler extends ChannelInboundHandlerAdapter {
    private final CaptureEngine engine;
    private final ConnectionInfo info;
    private final PipelineLayout layout;
    private final Direction direction;
    private boolean failed;

    CaptureInboundHandler(CaptureEngine engine, ConnectionInfo info, PipelineLayout layout, Direction direction) {
        this.engine = engine;
        this.info = info;
        this.layout = layout;
        this.direction = direction;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        info.remote = describe(ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!failed && msg instanceof ByteBuf buf) {
            try {
                fillRemote(ctx);
                engine.onFrame(info, direction, new ByteBufReader(buf));
            } catch (Throwable t) {
                fail(t);
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (layout.reanchorEvent().test(evt)) {
            try {
                NettyInjector.anchor(ctx.pipeline(), engine, info, layout, direction);
            } catch (Throwable t) {
                fail(t);
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        engine.onClose(info);
        ctx.fireChannelInactive();
    }

    /** Netty completes a connect promise (where Velocity writes its first packet) before it fires channelActive. */
    private void fillRemote(ChannelHandlerContext ctx) {
        if (info.remote == null) {
            info.remote = describe(ctx.channel().remoteAddress());
        }
    }

    private void fail(Throwable t) {
        if (!failed) {
            failed = true;
            info.ignored = true;
            engine.log().warn("capture disabled for connection " + info.id + ": " + t);
        }
    }

    static String describe(SocketAddress address) {
        if (address instanceof InetSocketAddress inet) {
            return inet.getHostString() + ":" + inet.getPort();
        }
        return address == null ? null : address.toString();
    }
}
