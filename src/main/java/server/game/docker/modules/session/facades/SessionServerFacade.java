package server.game.docker.modules.session.facades;

import io.netty.channel.ChannelId;
import server.game.docker.modules.session.pdus.SessionPDU;
import server.game.docker.modules.state.facades.StateServerFacade;
import server.game.docker.ship.parents.facades.ServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Map;

public class SessionServerFacade extends ServerFacade<SessionPDU> {
    private boolean ended = false;

    public boolean isEnded() {
        return ended;
    }

    public void receiveSessionStart(final ChannelId... players) {
        final var sessionPDU = new SessionPDU((byte) SessionPDU.SessionFlag.START.ordinal());
        multicastPDUToClientChannelIds(sessionPDU, players);
    }

    public void receiveSessionEnd(final ChannelId... players) {
        final var sessionPDU = new SessionPDU((byte) SessionPDU.SessionFlag.STOP.ordinal());
        multicastPDUToClientChannelIds(sessionPDU, players);
        ended = true;
    }

    public void receiveSessionTick(
            final ChannelId playerId,
            final Map<ChannelId, String> playerLobby,
            final PDU protocolDataUnit,
            final StateServerFacade stateServerFacade
            ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
