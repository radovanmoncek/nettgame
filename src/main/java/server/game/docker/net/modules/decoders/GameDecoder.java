package server.game.docker.net.modules.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.net.parents.pdus.PDU;
import server.game.docker.net.enums.PDUType;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Stream;

public class GameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        //todo: Check protocol identifier, else drop
        if (in.readableBytes() < 3)
            return;

        if (
                (int) in.readUnsignedByte() != 'F'
                        || (int) in.readUnsignedByte() != 'E'
                        || (int) in.readUnsignedByte() != 'D'
        ) {
            return;
        }

        //Await "magic" byte for PDUType identification
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();

        PDUType type = PDUType.valueOf((byte) in.readUnsignedByte());

        //If PDU tagged invalid, drop
        if (type.equals(PDUType.INVALID)) {
            in.resetReaderIndex();
            return;
        }

        in.markReaderIndex();

        //Determine, if any PDU body data are to be transported (their length is based on an Integer length identifier (4B) which has to be awaited)
        long bodyLength;
        if ((in.readableBytes() >= 8) && (bodyLength = in.readLong()) != 0 && (in.readableBytes() - 8 < bodyLength)) {
            in.resetReaderIndex();
            return;
        }

        byte[] outBytes = new byte[in.readableBytes()];
        in.readBytes(outBytes);
        out.add(ByteBuffer.allocate(1 + outBytes.length).put((byte) type.ordinal()).put(outBytes));
    }
}
