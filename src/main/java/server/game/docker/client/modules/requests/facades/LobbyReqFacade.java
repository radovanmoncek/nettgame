package server.game.docker.client.modules.requests.facades;

import io.netty.channel.Channel;
import server.game.docker.client.GameClient;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.parents.pdus.PDU;

public class LobbyReqFacade {
    private Channel clientChannel;

    public final void createLobby() {
        final var lobbyRequest = new PDULobbyReq();
        lobbyRequest.setActionFlag(PDULobbyReq.CREATE);
        writeToChannel(lobbyRequest);
    }

    public final void joinLobby(final Long lobbyID) {
        final var lobbyReq = new PDULobbyReq();
        lobbyReq.setLobbyID(lobbyID);
        lobbyReq.setActionFlag(PDULobbyReq.JOIN);
        writeToChannel(lobbyReq);
    }

    public void leaveLobby(GameClient gameClient){}

    private void writeToChannel(final PDU protocolDataUnit) {
        clientChannel.writeAndFlush(protocolDataUnit);
    }
}
