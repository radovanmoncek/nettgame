package server.game.docker.client.net.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.dto.JoinLobbyReq;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.PDUHandler;
import server.game.docker.net.pdu.PDUType;

public class GameClientHandler extends ChannelInboundHandlerAdapter {
//    private ByteBuf buf;
    private final PDUHandler actionPDUHandler;

    public GameClientHandler(PDUHandler actionPDUHandler) {
        super();
        this.actionPDUHandler = actionPDUHandler
                .registerPDU(PDUType.JOIN, new LocalPipeline() {
                    @Override
                    public Object decode(ByteBuf in) {
                        JoinLobbyReq out = new JoinLobbyReq();
                        in = in.slice(2, in.readableBytes());
                        out.setLobbyID(in.readLong());
                        return out;
                    }

                    @Override
                    public ByteBuf encode(Object in) {
                        JoinLobbyReq joinLobbyReq = (JoinLobbyReq) in;
                        return Unpooled.wrappedBuffer(new byte[]{0, PDUType.JOIN.getID(), joinLobbyReq.getLobbyID().byteValue()});
                    }

                    @Override
                    public void perform(PDU p) {
                        System.out.println(p);
                    }
                });
    }

//    @Override
//    public void handlerAdded(ChannelHandlerContext ctx) {
//        buf = ctx.alloc().buffer(4);
//    }
//
//    @Override
//    public void handlerRemoved(ChannelHandlerContext ctx) {
//        buf.release();
//        buf = null;
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        GameDataDecoder.TimeExampleDTO m = (GameDataDecoder.TimeExampleDTO) msg;
        PDU m = (PDU) msg;
//        actionPDUHandler.map(m.getGameDataPDUType()).perform(m);
        actionPDUHandler.receive(m);
//        buf.writeBytes(m);
//        m.release();
//        if(buf.readableBytes() < 4)
//            return;
//        try{
//            long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L;
//        System.out.println(/*new Date(currentTimeMillis)*/m);
//        ctx.close();
//        }
//        finally {
//            m.release();
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
