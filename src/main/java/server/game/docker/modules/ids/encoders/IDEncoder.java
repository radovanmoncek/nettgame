package server.game.docker.modules.ids.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import server.game.docker.GameServerInitializer;
import server.game.docker.modules.ids.pdus.PDUID;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

public final class IDEncoder extends ChannelOutboundHandlerAdapter {
    /**
     * @param ctx {@link ChannelHandlerContext}
     * @param msg {@link Object}
     * @param promise {@link ChannelPromise}
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        byte [] encodedBody = ((ByteBuf) msg).array();
        ByteBuf buf = Unpooled.buffer(3 + encodedBody.length);

        buf.writeBytes(new byte[]{'G', 'D', 'P'});

        buf.writeBytes(encodedBody);

        ctx.write(buf, promise);
    }

    public static class IDDecoderInner implements GameServerInitializer.PDUHandlerEncoder {
        @Override
        public void encode(PDU in, Channel out) {
            PDUID iD = (PDUID) in;
            ByteBuf byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES)
                    .writeByte(PDUType.ID.oneBasedOrdinal())
                    .writeLong(Long.BYTES)
                    .writeLong(iD.getNewClientID());
            out.writeAndFlush(byteBuf);
        }
    }
}
