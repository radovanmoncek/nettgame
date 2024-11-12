package server.game.docker.modules.lobby.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;
import server.game.docker.ship.enums.PDUType;

import java.util.List;

public final class LobbyRequestDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        if(byteBuf.readableBytes() < Byte.BYTES + Long.BYTES)
            return;

        byteBuf.markReaderIndex();

        final var type = PDUType.valueOf((byte) byteBuf.readUnsignedByte());

        if(type.equals(PDUType.INVALID))
            throw new CorruptedFrameException(String.format("Received an invalid PDUType: %s", type));

        if(!type.equals(PDUType.LOBBYREQUEST)) {
            byteBuf.resetReaderIndex();
            return;
        }

        if((byteBuf.readableBytes() < byteBuf.readLong())){
            byteBuf.resetReaderIndex();
            return;
        }

        final var lobbyReq = new LobbyRequestPDU();
        lobbyReq.setActionFlag(byteBuf.readByte());
        if(lobbyReq.getActionFlag() == 1)
            lobbyReq.setLeaderId(byteBuf.readLong());
        out.add(lobbyReq);
    }
}
