package server.game.docker.modules.beacons.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.ship.enums.PDUType;

public final class LobbyBeaconHandlerEncoder extends MessageToByteEncoder<PDULobbyBeacon> {
    @Override
    public void encode(ChannelHandlerContext channelHandlerContext, PDULobbyBeacon lobbyBeacon, ByteBuf out) {
        final var byteBuf = Unpooled.buffer(2 * Long.BYTES + 4 * Byte.BYTES)
                .writeByte(PDUType.LOBBYBEACON.oneBasedOrdinal())
                .writeLong(Long.BYTES + 2 * Byte.BYTES + 1)
                .writeLong(lobbyBeacon.getLobbyID())
                .writeByte(lobbyBeacon.getLobbyCurOccupancy())
                .writeByte(lobbyBeacon.getLobbyMaxOccupancy())
                .writeBoolean(lobbyBeacon.getLobbyListRefresh());

        channelHandlerContext.writeAndFlush(byteBuf);
    }
}
