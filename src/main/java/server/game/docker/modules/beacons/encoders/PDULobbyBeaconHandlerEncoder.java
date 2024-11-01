package server.game.docker.modules.beacons.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.ship.enums.PDUType;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.ship.parents.pdus.PDU;

public class PDULobbyBeaconHandlerEncoder implements GameServerInitializer.PDUHandlerEncoder {
    @Override
    public void encode(PDU in, Channel out) {
        PDULobbyBeacon lobbyBeacon = (PDULobbyBeacon) in;
        ByteBuf byteBuf = Unpooled.buffer(2 * Long.BYTES + 4 * Byte.BYTES)
                .writeByte(PDUType.LOBBYBEACON.oneBasedOrdinal())
                .writeLong(Long.BYTES + 2 * Byte.BYTES + 1)
                .writeLong(lobbyBeacon.getLobbyID())
                .writeByte(lobbyBeacon.getLobbyCurOccupancy())
                .writeByte(lobbyBeacon.getLobbyMaxOccupancy())
                .writeBoolean(lobbyBeacon.getLobbyListRefresh());

        out.writeAndFlush(byteBuf);
    }
}
