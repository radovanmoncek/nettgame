package server.game.docker.modules.requests.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.enums.PDUType;

import java.util.List;

public class LobbyRequestDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        if(byteBuf.readableBytes() < 5)
            return;

        byteBuf.markReaderIndex();

        final var type = PDUType.valueOf((byte) byteBuf.readUnsignedByte());

        if(type.equals(PDUType.INVALID))
            throw new CorruptedFrameException(String.format("Received an invalid PDUType: %s", type));

        if(!type.equals(PDUType.LOBBYREQUEST)) {
            byteBuf.readerIndex(5);
            return;
        }

        if((byteBuf.readableBytes() < byteBuf.readLong())){
            byteBuf.resetReaderIndex();
            return;
        }

        final var lobbyReq = new PDULobbyReq();
        lobbyReq.setActionFlag(byteBuf.readByte());
        if(lobbyReq.getActionFlag() == 1)
            lobbyReq.setLobbyID(byteBuf.readLong());
        out.add(lobbyReq);
    }
}
