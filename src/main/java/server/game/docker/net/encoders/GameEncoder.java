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
     * @param ctx
     * @param msg
     * @param promise
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
//        GameDataDecoder.TimeExampleDTO m = (GameDataDecoder.TimeExampleDTO) msg;
//        ByteBuf encoded = ctx.alloc().buffer(4);
//        encoded.writeInt((int) m.getValue());
//        ctx.write(encoded, promise);
//        GameDataPDUAction gameDataPDUAction = (GameDataPDUAction) msg;
//        Vector<Object> m = (Vector<Object>) msg;
//        PDUType type = (PDUType) m.get(0);
//        Object data = m.get(1);
        PDU p = (PDU) msg; //toto: will inject PDUType bytes and LP will only return byte []
        byte [] encodedBody = /*actionPDUHandler.map(p.getGameDataPDUType()).encode(p.getData()).array()*/p.getByteBuf().array();
        ByteBuf buf = Unpooled.buffer(1 + (!p.getPDUType().isEmpty()/*encodedBody.length != 0*/? 4 : 0) + /*p.getGameDataPDUType().getMinimumTransportSize()*/encodedBody.length)/*actionHandler.map(body.getGameDataPDUType()).encode(body.getData())*/;
        //Tag traffic with PDUType identifier header part
        buf.writeByte(p.getPDUType().getID());
//        if(p.getGameDataPDUType().isVariableLen())
        //Tag traffic with length info header part
        if(encodedBody.length != 0)
            buf.writeInt(encodedBody.length);

        //Write the actual data
        buf.writeBytes(encodedBody);

//        byte [] temp = new byte[buf.readableBytes()];
//        for(int i = 0; i < buf.readableBytes(); i++)
//            temp[i] = buf.getByte(i);
        //Send data ByteBuff for further processing
        ctx.write(buf, promise);
    }
}
