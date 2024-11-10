package server.game.docker.client.modules.requests.facades;

import server.game.docker.client.ship.parents.ClientFacade;
import server.game.docker.modules.requests.pdus.LobbyReqPDU;

/**
 * <p>
 *     Provides client side lobby functionality for outbound actions.
 * </p>
 */
public class LobbyRequestClientFacade extends ClientFacade<LobbyReqPDU> {
    /**
     * <p>
     *     Sends a {@link LobbyReqPDU} to the server with the actionFlag attribute set to 0 (CREATE).
     * </p>
     */
    public void createLobby() {
        final var lobbyRequest = new LobbyReqPDU();
        lobbyRequest.setActionFlag(LobbyReqPDU.CREATE);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void joinLobby(final Long lobbyID) {
        final var lobbyReq = new LobbyReqPDU();
        lobbyReq.setLobbyID(lobbyID);
        lobbyReq.setActionFlag(LobbyReqPDU.JOIN);
        unicastPDUToServerChannel(lobbyReq);
    }

    public void leaveLobby(){
        final var lobbyRequest = new LobbyReqPDU();
        lobbyRequest.setActionFlag(LobbyReqPDU.LEAVE);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public Long getLobbyID() {
        return null;
    }
}
