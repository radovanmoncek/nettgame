package server.game.docker.net.modules.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.net.enums.PDUType;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        //todo: Check protocol identifier, else drop
        if (in.readableBytes() < 8) {
            return;
        }

        in.markReaderIndex();

        byte [] protocolIdentifier = new byte[3];
        if (
                (protocolIdentifier[0] = (byte) in.readUnsignedByte()) != 'G'
                        || (protocolIdentifier[1] = (byte) in.readUnsignedByte()) != 'D'
                        || (protocolIdentifier[2] = (byte) in.readUnsignedByte()) != 'P'
        ) {
            in.resetReaderIndex();
            throw new CorruptedFrameException(String.format("Corrupted frame: %s", Arrays.toString(protocolIdentifier)));
        }

        PDUType type = PDUType.valueOf((byte) in.readUnsignedByte());

        if (type.equals(PDUType.INVALID)) {
            in.resetReaderIndex();
            return;
        }

        long bodyLength;
        if ((bodyLength = in.readLong()) != 0 && (in.readableBytes() < bodyLength)) {
            in.resetReaderIndex();
            return;
        }

        byte[] outBytes = new byte[in.readableBytes()];
        in.readBytes(outBytes);
        out.add(ByteBuffer.allocate(Byte.BYTES + outBytes.length).put((byte) type.oneBasedOrdinal()).put(outBytes));
    }
}
