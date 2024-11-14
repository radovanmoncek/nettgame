package server.game.docker.client.modules.lobby.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import server.game.docker.modules.lobby.pdus.LobbyUpdatePDU;
import server.game.docker.ship.enums.PDUType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LobbyUpdateDecoder extends ByteToMessageDecoder {
    private static final int MAX_USERNAME_LENGTH = 8;

    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        final var type = PDUType.valueOf((byte) in.readUnsignedByte());

        if(!type.equals(PDUType.LOBBYUPDATE)) {
            in.resetReaderIndex();
//            channelHandlerContext.fireChannelRead(in);
            return;
        }

        if (in.readableBytes() < in.readLong()) {
            in.resetReaderIndex();
            return;
        }

        final var lobbyUpdate = new LobbyUpdatePDU();
        lobbyUpdate.setStateFlag((byte) in.readUnsignedByte());
        lobbyUpdate.setLeaderId(in.readLong());
        final var lobbyMembers = new ArrayList<String>();
        while (in.readableBytes() >= MAX_USERNAME_LENGTH) {
            lobbyMembers.add(in.toString(in.readerIndex(), MAX_USERNAME_LENGTH, Charset.defaultCharset()).trim());
            if(in.writerIndex() >= in.readerIndex() + Long.BYTES) {
                in.readerIndex(in.readerIndex() + Long.BYTES);
            }
        }
        lobbyUpdate.setMembers(lobbyMembers);

        out.add(lobbyUpdate);
    }
}
