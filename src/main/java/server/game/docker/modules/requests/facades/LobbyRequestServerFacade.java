package server.game.docker.modules.requests.facades;

import server.game.docker.modules.requests.pdus.LobbyReqPDU;
import server.game.docker.ship.parents.facades.ServerFacade;

public class LobbyRequestServerFacade extends ServerFacade<LobbyReqPDU> {
    public void receiveCreateRequest(final Long clientID, final Long lobbyID) {
        throw new UnsupportedOperationException("Method is not implemented.");
    };
    public void receiveJoinRequest(final Long clientID, final Long lobbyID){
        throw new UnsupportedOperationException("Method is not implemented.");
    };
    public void receiveLeaveRequest(final Long clientID, final Long lobbyID){
        throw new UnsupportedOperationException("Method is not implemented.");
    };
}
