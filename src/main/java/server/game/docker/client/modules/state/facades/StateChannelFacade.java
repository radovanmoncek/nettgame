package server.game.docker.client.modules.state.facades;

import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.client.ship.parents.facades.ChannelFacade;

import java.util.List;

/**
 * Example facade for communication via {@link StateRequestPDU}.
 */
public class StateChannelFacade extends ChannelFacade<StateRequestPDU> {

    public void requestState(final Integer x, final Integer y) {
        unicastPDUToServerChannel(new StateRequestPDU(x, y));
    }

    public void receiveState(final List<GameEntity> gameEntities) {
         throw new UnsupportedOperationException("Not supported yet.");
    }

    public record GameEntity(Integer x, Integer y) {
    }
}
