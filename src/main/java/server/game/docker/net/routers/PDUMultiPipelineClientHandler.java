package server.game.docker.net.routers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.modules.requests.pdus.PDULobbyReq;

import java.nio.ByteBuffer;

public class PDUMultiPipelineClientHandler extends ChannelInboundHandlerAdapter {
    private final RouterHandler multiPipeline;

    public PDUMultiPipelineClientHandler(RouterHandler multiPipeline) {
        this.multiPipeline = multiPipeline;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuffer byteBuffer = ((ByteBuffer) msg).position(0);
        multiPipeline.route(PDUType.valueOf(byteBuffer.get()), Unpooled.wrappedBuffer(byteBuffer), ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
