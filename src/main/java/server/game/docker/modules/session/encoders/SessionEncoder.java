package server.game.docker.modules.session.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.session.pdus.SessionPDU;

public class SessionEncoder extends MessageToByteEncoder<SessionPDU> {
    @Override
    protected void encode(ChannelHandlerContext ctx, SessionPDU msg, ByteBuf out) {
        final var outBytes = Unpooled
                .buffer(Byte.BYTES + Long.BYTES)
                .writeByte(SessionPDU.PROTOCOL_IDENTIFIER)
                .writeLong(Byte.BYTES)
                .writeByte(msg.sessionFlag());

        ctx.writeAndFlush(outBytes);
    }
}
