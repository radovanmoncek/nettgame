package server.game.docker.net.modules.updates.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.modules.updates.pdus.PDULobbyUpdate;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;

import java.util.ArrayList;
import java.util.Collection;

public class PDULobbyUpdateDecoder implements PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf in, Channel channel, PDUInboundHandler out) {
            PDULobbyUpdate lobbyUpdate = new PDULobbyUpdate();
            lobbyUpdate.setStateFlag((byte) in.readUnsignedByte());
            lobbyUpdate.setLobbyId(in.readLong());
            lobbyUpdate.setLeader(in.readBoolean());
            final Collection<Long> lobbyMembers = new ArrayList<>();
            while (in.readableBytes() >= 8) {
                if(in.readLong() == 0)
                    break;
                lobbyMembers.add(in.readLong());
            }
            lobbyUpdate.setMembers(lobbyMembers);

            out.handle(lobbyUpdate);
    }
}
