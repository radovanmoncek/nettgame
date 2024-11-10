package server.game.docker.client.modules.beacons.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;

public class LobbyBeaconHandlerDecoder implements GameServerInitializer.PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf in, Channel channel, GameServerInitializer.PDUInboundHandler out) {
        PDULobbyBeacon PDULobbyBeacon = new PDULobbyBeacon();
        PDULobbyBeacon.setLobbyID(in.readLong());
        PDULobbyBeacon.setLobbyCurOccupancy(in.readByte());
        PDULobbyBeacon.setLobbyMaxOccupancy(in.readByte());
        PDULobbyBeacon.setLobbyListRefresh(in.readBoolean());
        out.handle(PDULobbyBeacon);
    }
}
