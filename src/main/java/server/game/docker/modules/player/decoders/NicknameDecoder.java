package server.game.docker.modules.player.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.player.pdus.NicknamePDU;
import server.game.docker.ship.enums.PDUType;

import java.nio.charset.Charset;
import java.util.List;

public final class NicknameDecoder extends ByteToMessageDecoder {
    private final int MAX_USERNAME_LENGTH = 8;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < (Byte.BYTES + Long.BYTES)) {
            return;
        }

        in.markReaderIndex();

        final var type = PDUType.valueOf((byte) in.readUnsignedByte());

        if (type.equals(PDUType.INVALID)) {
            throw new CorruptedFrameException(String.format("Invalid PDU type received: %s", in));
        }

        if(!type.equals(PDUType.USERNAME)) {
            in.resetReaderIndex();
            channelHandlerContext.fireChannelRead(in.retain());
            return;
        }

        if (in.readableBytes() < in.readLong()) {
            in.resetReaderIndex();
            return;
        }

        final var usernamePDU = new NicknamePDU();
        usernamePDU.setNewClientUsername(in.toString(in.readerIndex(), MAX_USERNAME_LENGTH, Charset.defaultCharset()).trim());
        in.readerIndex(in.readerIndex() + MAX_USERNAME_LENGTH);
        out.add(usernamePDU);
    }
}
