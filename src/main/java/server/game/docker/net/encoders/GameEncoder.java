package server.game.docker.net.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import server.game.docker.net.pdu.PDU;

public class GameEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * @param ctx {@link ChannelHandlerContext}
     * @param msg {@link Object}
     * @param promise {@link ChannelPromise}
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PDU inPDU = (PDU) msg;//todo: will inject PDUType bytes and LP will only return byte []
        byte [] encodedBody = ((ByteBuf) inPDU.getData()).array();
        ByteBuf buf = Unpooled.buffer(inPDU.getProtocolID().length + 1 + (!inPDU.getPDUType().isEmpty()? 4 : 0) + encodedBody.length);

        //Tag PDU with (GD) GameData protocol
        buf.writeBytes(new byte[]{'F', 'E', 'D'});

        //Tag traffic with PDUType identifier header part
        buf.writeByte(inPDU.getPDUType().getID());

        //Tag traffic with length info header part
        //todo: write always!!!!
        if(encodedBody.length != 0)
            buf.writeInt(encodedBody.length);

        //Write the actual data
        buf.writeBytes(encodedBody);

        //Send data ByteBuff for further processing
        ctx.write(buf, promise);
    }
}
