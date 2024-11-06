package server.game.docker.client.modules.updates.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.updates.pdus.PDULobbyUpdate;

import java.util.ArrayList;
import java.util.List;

public class LobbyUpdateDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
            final var lobbyUpdate = new PDULobbyUpdate();
            lobbyUpdate.setStateFlag((byte) in.readUnsignedByte());
            lobbyUpdate.setLobbyId(in.readLong());
            lobbyUpdate.setLeader(in.readBoolean());
            final var lobbyMembers = new ArrayList<Long>();
            while (in.readableBytes() >= 8) {
                if(in.readLong() == 0)
                    break;
                lobbyMembers.add(in.readLong());
            }
            lobbyUpdate.setMembers(lobbyMembers);

            out.add(lobbyUpdate);
    }
}
