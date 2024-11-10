package server.game.docker.modules.beacons.facades;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import server.game.docker.modules.beacons.pdus.PDULobbyBeacon;
import server.game.docker.ship.parents.facades.ServerFacade;

public class LobbyBeaconServerFacade extends ServerFacade<PDULobbyBeacon> {
    public void advertiseLobbyToChannelsExceptLobbyChannelGroup(
            final ChannelGroup lobbyClientChannels,
            final Long lobbyID,
            final Byte currentOccupancy,
            final Byte maximumOccupancy,
            final Boolean refreshLobbyList
    ){
        final var lobbyBeacon = new PDULobbyBeacon();
        lobbyBeacon.setLobbyID(lobbyID);
        lobbyBeacon.setLobbyCurOccupancy(currentOccupancy);
        lobbyBeacon.setLobbyMaxOccupancy(maximumOccupancy);
        lobbyBeacon.setLobbyListRefresh(refreshLobbyList);
        multicastPDUToChannelGroup(lobbyBeacon, lobbyClientChannels);
    }

    public void advertiseLobbyToChannel(
            final Channel clientChannel,
            final Long lobbyID,
            final Byte currentOccupancy,
            final Byte maximalOccupancy,
            final Boolean refreshLobbyList
    ){
        final var lobbyBeacon = new PDULobbyBeacon();
        lobbyBeacon.setLobbyID(lobbyID);
        lobbyBeacon.setLobbyCurOccupancy(currentOccupancy);
        lobbyBeacon.setLobbyMaxOccupancy(maximalOccupancy);
        lobbyBeacon.setLobbyListRefresh(refreshLobbyList);
        unicastPDUToClientChannel(lobbyBeacon, clientChannel);
    }
}
