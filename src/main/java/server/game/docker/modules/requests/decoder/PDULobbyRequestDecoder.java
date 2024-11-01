package server.game.docker.modules.requests.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.modules.requests.pdus.PDULobbyReq;

public class PDULobbyRequestDecoder implements GameServerInitializer.PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf byteBuf, Channel channel, GameServerInitializer.PDUInboundHandler handler) {
        PDULobbyReq lobbyReq = new PDULobbyReq();
        lobbyReq.setActionFlag(byteBuf.readByte());
        lobbyReq.setLobbyID(byteBuf.readLong());
        handler.handle(lobbyReq, channel);
    }
}
