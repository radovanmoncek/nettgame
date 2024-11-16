package server.game.docker.client.modules.sessions.facades;

import server.game.docker.client.ship.parents.facades.ClientFacade;
import server.game.docker.modules.session.pdus.SessionPDU;

public class SessionClientFacade extends ClientFacade<SessionPDU> {
    public void requestStartSession() {
        unicastPDUToServerChannel(new SessionPDU((byte) SessionPDU.SessionFlag.START.ordinal()));
    }

    public void requestStopSession() {
        unicastPDUToServerChannel(new SessionPDU((byte) SessionPDU.SessionFlag.STOP.ordinal()));
    }

    public void receiveStartSessionResponse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void receiveStopSessionResponse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
