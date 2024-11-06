package server.game.docker.client.modules.requests.facades;

import io.netty.channel.Channel;
import server.game.docker.client.GameClient;
import server.game.docker.modules.requests.pdus.PDULobbyReq;
import server.game.docker.ship.parents.pdus.PDU;

/**
 * <p>
 *     Provides client side lobby functionality for outbound actions.
 * </p>
 */
public class LobbyReqClientFacade {
    private Channel clientChannel;

    /**
     * <p>
     *     Sends a {@link PDULobbyReq} to the server with the actionFlag attribute set to 0 (CREATE).
     * </p>
     */
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

    public void leaveLobby(GameClient gameClient){
        final var lobbyRequest = new PDULobbyReq();
        lobbyRequest.setActionFlag(PDULobbyReq.LEAVE);
        writeToChannel(lobbyRequest);
    }

    private void writeToChannel(final PDU protocolDataUnit) {
        clientChannel.writeAndFlush(protocolDataUnit);
    }
}
