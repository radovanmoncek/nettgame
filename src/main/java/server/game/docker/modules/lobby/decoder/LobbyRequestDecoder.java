package server.game.docker.modules.lobby.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;

import java.util.List;

public final class LobbyRequestDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = in.readUnsignedByte();

        if(type != LobbyRequestPDU.PROTOCOL_IDENTIFIER) {
            in.resetReaderIndex();
            channelHandlerContext.fireChannelRead(in.retain());
            return;
        }

        if((in.readableBytes() < in.readLong())){
            in.resetReaderIndex();
            return;
        }

        final var lobbyRequestFlag = in.readUnsignedByte();

        final var lobbyReq = new LobbyRequestPDU((byte) lobbyRequestFlag, lobbyRequestFlag == LobbyRequestPDU.LobbyRequestFlag.JOIN.ordinal()? in.readLong() : null);
        out.add(lobbyReq);
    }
}
