package server.game.docker.client.net.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.net.enums.PDUType;
import server.game.docker.net.pipelines.PDUMultiPipeline;

import java.nio.ByteBuffer;

public class GameClientHandler extends ChannelInboundHandlerAdapter {
    private final PDUMultiPipeline multiPipeline;

    public GameClientHandler(PDUMultiPipeline multiPipeline) {
        this.multiPipeline = multiPipeline;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuffer byteBuffer = ((ByteBuffer) msg).position(0);
        multiPipeline.ingest(PDUType.valueOf(byteBuffer.get()), Unpooled.wrappedBuffer(byteBuffer), ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
