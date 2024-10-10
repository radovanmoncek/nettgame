package server.game.docker.net.modules.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import server.game.docker.net.parents.pdus.PDU;

public class GameEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * @param ctx {@link ChannelHandlerContext}
     * @param msg {@link Object}
     * @param promise {@link ChannelPromise}
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        byte [] encodedBody = ((ByteBuf) msg).array();
        ByteBuf buf = Unpooled.buffer(3 + encodedBody.length);

        /*--------header--------*/
        //Tag PDU with (GD) GameData protocol
        buf.writeBytes(new byte[]{'F', 'E', 'D'});

        /*--------body--------*/
        //Write the actual data
        buf.writeBytes(encodedBody);

        //Send data ByteBuff for further processing
        ctx.write(buf, promise);
    }
}
