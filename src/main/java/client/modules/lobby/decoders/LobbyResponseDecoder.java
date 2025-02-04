package client.modules.lobby.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import container.game.docker.modules.lobby.pdus.LobbyResponseProtocolDataUnit;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LobbyResponseDecoder extends ByteToMessageDecoder {
    private static final int MAX_USERNAME_LENGTH = 8;

    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if(type != LobbyResponseProtocolDataUnit.PROTOCOL_IDENTIFIER) {
            in.resetReaderIndex();
            channelHandlerContext.fireChannelRead(in.retain());
            return;
        }

        if (in.readableBytes() < in.readLong()) {
            in.resetReaderIndex();
            return;
        }

        final var lobbyUpdate = new LobbyResponseProtocolDataUnit((byte) in.readUnsignedByte(), in.readLong(), new ArrayList<>());
        final var lobbyMembers = new ArrayList<String>();
        while (in.readableBytes() >= MAX_USERNAME_LENGTH) {
            lobbyMembers.add(in.toString(in.readerIndex(), MAX_USERNAME_LENGTH, Charset.defaultCharset()).trim());
            if(in.writerIndex() >= in.readerIndex() + Long.BYTES) {
                in.readerIndex(in.readerIndex() + Long.BYTES);
            }
        }
        lobbyUpdate.members().addAll(lobbyMembers);

        out.add(lobbyUpdate);
    }
}
