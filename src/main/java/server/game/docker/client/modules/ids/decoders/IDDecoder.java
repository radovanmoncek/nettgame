package server.game.docker.client.modules.ids.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.ids.pdus.PDUID;
import server.game.docker.ship.enums.PDUType;

import java.util.List;

public final class IDDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 5) {
            return;
        }

        in.markReaderIndex();

        final var type = PDUType.valueOf((byte) in.readUnsignedByte());

        if (type.equals(PDUType.INVALID)) {
            throw new CorruptedFrameException(String.format("Invalid PDU type received: %s", in));
        }

        if(!type.equals(PDUType.ID)) {
            in.readerIndex(5);
//            channelHandlerContext.pipeline().
            return;
        }

        long bodyLength;
        if ((bodyLength = in.readLong()) != 0 && (in.readableBytes() < bodyLength)) {
            in.resetReaderIndex();
            return;
        }

        final var identifier = new PDUID();
        identifier.setNewClientID(in.readLong());
        out.add(identifier);
    }
}
