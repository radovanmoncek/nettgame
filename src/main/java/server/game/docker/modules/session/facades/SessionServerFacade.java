package server.game.docker.modules.session.facades;

import server.game.docker.modules.session.pdus.SessionResponsePDU;
import server.game.docker.ship.parents.facades.ServerFacade;

public class SessionServerFacade extends ServerFacade<SessionResponsePDU> {
    private boolean ended = false;

    public boolean isEnded() {
        return ended;
    }

    public void receivedServerTick() {

    }
}
