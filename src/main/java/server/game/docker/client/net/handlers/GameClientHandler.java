package server.game.docker.client.net.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.client.ClientAPIEventType;
import server.game.docker.net.LocalPipeline;
import server.game.docker.net.pdu.PDU;
import server.game.docker.net.PDUHandler;
import server.game.docker.net.pdu.PDUType;

import java.util.Map;

public class GameClientHandler extends ChannelInboundHandlerAdapter {
    private final PDUHandler actionPDUHandler;

    public GameClientHandler(PDUHandler actionPDUHandler, Map<PDUType, LocalPipeline> localPipelines, Map<ClientAPIEventType, Object> eventMappings) {
        super();
        this.actionPDUHandler = actionPDUHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PDU m = (PDU) msg;
        actionPDUHandler.receive(m);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
