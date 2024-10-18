package server.game.docker.net.routers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public final class RouterEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * @param ctx {@link ChannelHandlerContext}
     * @param msg {@link Object}
     * @param promise {@link ChannelPromise}
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        byte [] encodedBody = ((ByteBuf) msg).array();
        ByteBuf buf = Unpooled.buffer(3 + encodedBody.length);

        buf.writeBytes(new byte[]{'G', 'D', 'P'});

        buf.writeBytes(encodedBody);

        ctx.write(buf, promise);
    }
}
