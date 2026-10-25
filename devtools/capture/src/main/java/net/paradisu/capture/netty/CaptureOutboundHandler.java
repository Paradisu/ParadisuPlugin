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
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.paradisu.capture.core.CaptureEngine;
import net.paradisu.capture.core.ConnectionInfo;
import net.paradisu.capture.core.Direction;

/** Taps outbound frames just after they were encoded; forwards everything unchanged. */
final class CaptureOutboundHandler extends ChannelOutboundHandlerAdapter {
    private final CaptureEngine engine;
    private final ConnectionInfo info;
    private final Direction direction;
    private boolean failed;

    CaptureOutboundHandler(CaptureEngine engine, ConnectionInfo info, Direction direction) {
        this.engine = engine;
        this.info = info;
        this.direction = direction;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!failed) {
            try {
                if (info.remote == null) {
                    info.remote = CaptureInboundHandler.describe(ctx.channel().remoteAddress());
                }
                if (msg instanceof ByteBuf buf) {
                    engine.onFrame(info, direction, new ByteBufReader(buf));
                } else if (msg instanceof ByteBufHolder holder) {
                    engine.onFrame(info, direction, new ByteBufReader(holder.content()));
                }
            } catch (Throwable t) {
                failed = true;
                info.ignored = true;
                engine.log().warn("capture disabled for connection " + info.id + ": " + t);
            }
        }
        ctx.write(msg, promise);
    }
}
