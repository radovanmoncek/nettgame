package server.game.docker.client.net.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.LocalPDUPipeline;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class GameClientHandler extends ChannelInboundHandlerAdapter {
    private final Map<PDUType, LocalPDUPipeline> localPDUPipelines;

    public GameClientHandler(Map<PDUType, LocalPDUPipeline> localPDUPipelines) {
        this.localPDUPipelines = localPDUPipelines;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PDU m = (PDU) msg;
        localPDUPipelines.get(m.getPDUType()).ingest(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
