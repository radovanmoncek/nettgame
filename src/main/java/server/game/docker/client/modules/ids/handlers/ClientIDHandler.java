package server.game.docker.client.modules.ids.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.game.docker.GameServerInitializer;
import server.game.docker.ship.enums.PDUType;

import java.nio.ByteBuffer;

public class ClientIDHandler extends ChannelInboundHandlerAdapter {
    private final GameServerInitializer.RouterHandler multiPipeline;

    public ClientIDHandler(GameServerInitializer.RouterHandler multiPipeline) {
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
