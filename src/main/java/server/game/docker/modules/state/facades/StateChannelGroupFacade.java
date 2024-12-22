package server.game.docker.modules.state.facades;

import io.netty.channel.ChannelId;
import server.game.docker.modules.state.pdus.StateResponsePDU;
import server.game.docker.ship.parents.facades.ChannelGroupFacade;

/**
 * Example
 */
public class StateChannelGroupFacade extends ChannelGroupFacade<StateResponsePDU> {

    public void respondToStateRequest(Integer x, Integer y, Integer x2, Integer y2, ChannelId... players) {
        multicastPDUToClientChannelIds(new StateResponsePDU(x, y, 0, x2, y2), players);
    }
}
