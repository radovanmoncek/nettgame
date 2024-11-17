package server.game.docker.modules.state.facades;

import io.netty.channel.ChannelId;
import server.game.docker.modules.state.pdus.StateResponsePDU;
import server.game.docker.ship.parents.facades.ServerFacade;

/**
 * Example
 */
public class StateServerFacade extends ServerFacade<StateResponsePDU> {

    public void respondToStateRequest(String playerNickname, Integer x, Integer y, ChannelId... players) {
        multicastPDUToClientChannelIds(new StateResponsePDU(playerNickname, x, y), players);
    }
}
