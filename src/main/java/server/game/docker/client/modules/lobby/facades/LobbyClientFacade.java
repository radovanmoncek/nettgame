package server.game.docker.client.modules.lobby.facades;

import server.game.docker.client.ship.parents.facades.ClientFacade;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;

import java.util.Collection;

/**
 * <p>
 *     Provides client side lobby functionality for outbound actions.
 * </p>
 */
public class LobbyClientFacade extends ClientFacade<LobbyRequestPDU> {
    /**
     * <p>
     *     Sends a {@link LobbyRequestPDU} to the server with the actionFlag attribute set to 0 (CREATE).
     * </p>
     */
    public void createLobby() {
        final var lobbyRequest = new LobbyRequestPDU();
        lobbyRequest.setActionFlag(LobbyRequestPDU.CREATE);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void joinLobby(final Long leaderChannelId) {
        final var lobbyReq = new LobbyRequestPDU();
        lobbyReq.setActionFlag(LobbyRequestPDU.JOIN);
        lobbyReq.setLeaderId(leaderChannelId);
        unicastPDUToServerChannel(lobbyReq);
    }

    public void leaveLobby(){
        final var lobbyRequest = new LobbyRequestPDU();
        lobbyRequest.setActionFlag(LobbyRequestPDU.LEAVE);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void receiveLobbyCreated(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyCreated is not implemented.");
    }

    public void receiveLobbyLeft(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyLeft is not implemented.");
    }

    public void receiveLobbyJoined(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyJoined is not implemented.");
    }
}
