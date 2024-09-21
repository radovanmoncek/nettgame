package server.game.docker.net.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.PDUHandler;

public class GameDataEncoder extends ChannelOutboundHandlerAdapter {
    private PDUHandler actionPDUHandler;

    public GameDataEncoder(PDUHandler actionPDUHandler) {
        this.actionPDUHandler = actionPDUHandler;
    }

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
        byte [] encodedBody = actionPDUHandler.map(p.getGameDataPDUType()).encode(p.getData()).array();
        ByteBuf buf = Unpooled.buffer(1 + (p.getGameDataPDUType().isVariableLen()? 4 : 0) + p.getGameDataPDUType().getMinimumTransportSize())/*actionHandler.map(body.getGameDataPDUType()).encode(body.getData())*/;
        buf.writeByte(p.getGameDataPDUType().getID());
        if(p.getGameDataPDUType().isVariableLen())
            buf.writeInt(encodedBody.length);
        buf.writeBytes(encodedBody);

        byte [] temp = new byte[buf.readableBytes()];
        for(int i = 0; i < buf.readableBytes(); i++)
            temp[i] = buf.getByte(i);
        ctx.write(buf, promise);
    }
}
