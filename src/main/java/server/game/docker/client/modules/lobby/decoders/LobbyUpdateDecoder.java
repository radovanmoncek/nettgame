package server.game.docker.client.modules.lobby.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import server.game.docker.modules.lobby.pdus.LobbyUpdatePDU;
import server.game.docker.ship.enums.PDUType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LobbyUpdateDecoder extends ByteToMessageDecoder {
    @Override
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < (Byte.BYTES + Long.BYTES)) {
            return;
        }

        in.markReaderIndex();

        final var type = PDUType.valueOf((byte) in.readUnsignedByte());

        if (type.equals(PDUType.INVALID)) {
            throw new CorruptedFrameException(String.format("Invalid PDU type received: %s", in));
        }

        if(!type.equals(PDUType.LOBBYUPDATE)) {
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
        while (in.readableBytes() >= 8) {
            if (in.readLong() == 0)
                break;
            lobbyMembers.add(in.toString(in.readerIndex(), 8, Charset.defaultCharset()));
            in.readerIndex(in.readerIndex() + 8);
        }
        lobbyUpdate.setMembers(lobbyMembers);

        out.add(lobbyUpdate);
    }
}
