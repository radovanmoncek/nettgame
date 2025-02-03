package server.game.docker.modules.session.facades;

import io.netty.channel.ChannelId;
import client.modules.state.models.StateRequestPDU;
import server.game.docker.modules.session.pdus.SessionPDU;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;
import server.game.docker.ship.parents.facades.ChannelGroupFacade;

import java.util.LinkedList;

public class SessionChannelGroupFacade extends ChannelGroupFacade<SessionPDU> {
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
            final LinkedList<ChannelId> playerLobby,
            final StateRequestPDU stateRequestPDU,
            final StateChannelGroupFacade stateServerFacade
            ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
