package server.game.docker.net.modules.beacons.decoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import server.game.docker.net.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.net.parents.decoders.PDUHandlerDecoder;
import server.game.docker.net.parents.handlers.PDUInboundHandler;

public class PDULobbyBeaconHandlerDecoder implements PDUHandlerDecoder {
    @Override
    public void decode(ByteBuf in, Channel channel, PDUInboundHandler out) {
        PDULobbyBeacon PDULobbyBeacon = new PDULobbyBeacon();
        PDULobbyBeacon.setLobbyID(in.readLong());
        PDULobbyBeacon.setLobbyCurOccupancy(in.readByte());
        PDULobbyBeacon.setLobbyMaxOccupancy(in.readByte());
        PDULobbyBeacon.setLobbyListRefresh(in.readBoolean());
        out.handle(PDULobbyBeacon);
    }
}
