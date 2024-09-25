package server.game.docker.net.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.PDUHandler;

public class GameEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * PDU:
     * <pre>
     *     ---------------------------------------------------
     *     | PDUType byte (1B)     |    dataLength int? (4B) |
     *     ---------------------------------------------------
     *     |                       data                      |
     *     ---------------------------------------------------
     * </pre>
     * @param ctx {@link ChannelHandlerContext}
     * @param msg {@link Object}
     * @param promise {@link ChannelPromise}
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PDU p = (PDU) msg; //todo: will inject PDUType bytes and LP will only return byte []
        byte [] encodedBody = p.getByteBuf().array();
        ByteBuf buf = Unpooled.buffer(1 + (!p.getPDUType().isEmpty()? 4 : 0) + encodedBody.length);
        //Tag traffic with PDUType identifier header part
        buf.writeByte(p.getPDUType().getID());
        //Tag traffic with length info header part
        if(encodedBody.length != 0)
            buf.writeInt(encodedBody.length);

        //Write the actual data
        buf.writeBytes(encodedBody);

        //Send data ByteBuff for further processing
        ctx.write(buf, promise);
    }
}
