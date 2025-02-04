package server.game.docker.modules.player.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public final class NicknameDecoder extends ByteToMessageDecoder {
    private static final byte MIN_PROTOCOL_IDENTIFIER = 0x1;
    private static final int MAX_USERNAME_LENGTH = 8;
    private static final byte MAX_PROTOCOL_IDENTIFIER = 0x10;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < (Byte.BYTES + Long.BYTES)) {
            return;
        }

        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if (type < MIN_PROTOCOL_IDENTIFIER || type > MAX_PROTOCOL_IDENTIFIER) {
            throw new CorruptedFrameException(String.format("Corrupted PDU received: %s", Arrays.toString(in.array())));
        }

        if(type != NicknameProtocolDataUnit.PROTOCOL_IDENTIFIER) {
            in.resetReaderIndex();
            channelHandlerContext.fireChannelRead(in.retain());
            return;
        }

        if (in.readableBytes() < in.readLong()) {
            in.resetReaderIndex();
            return;
        }

        final var usernamePDU = new NicknameProtocolDataUnit(in.toString(in.readerIndex(), MAX_USERNAME_LENGTH, Charset.defaultCharset()).trim());
        in.readerIndex(in.readerIndex() + MAX_USERNAME_LENGTH);
        out.add(usernamePDU);
    }
}
