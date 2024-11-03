package server.game.docker.modules.ids.encoders;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import server.game.docker.modules.ids.pdus.PDUID;
import server.game.docker.ship.enums.PDUType;

import java.util.List;

public final class IDEncoder extends MessageToMessageEncoder<PDUID> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PDUID id, List<Object> list) {
            final var byteBuf = Unpooled.buffer(Byte.BYTES + 2 * Long.BYTES)
                    .writeByte(PDUType.ID.oneBasedOrdinal())
                    .writeLong(Long.BYTES)
                    .writeLong(id.getNewClientID());

            channelHandlerContext.writeAndFlush(byteBuf);
    }
}
