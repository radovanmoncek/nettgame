package server.game.docker.client.net.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class GameClientHandler extends ChannelInboundHandlerAdapter {
    private final Map<PDUType, LocalPipeline> localPDUPipelines;

    public GameClientHandler(Map<PDUType, LocalPipeline> localPDUPipelines) {
        this.localPDUPipelines = localPDUPipelines;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PDU m = (PDU) msg;
        LocalPipeline p = localPDUPipelines.get(m.getPDUType());
        m.setData(p.decode((ByteBuf) m.getData()));
        p.handle(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
