package server.game.docker.client.modules.state.facades;

import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.client.ship.parents.facades.ClientFacade;

/**
 * Example facade for communication via {@link StateRequestPDU}.
 */
public class StateClientFacade extends ClientFacade<StateRequestPDU> {

    public void requestState(final Integer x, final Integer y) {
        unicastPDUToServerChannel(new StateRequestPDU(x, y));
    }

    public void receiveState(final String playerNickname, Integer x, Integer y){
         throw new UnsupportedOperationException("Not supported yet.");
    }
}
