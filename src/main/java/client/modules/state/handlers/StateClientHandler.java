package client.modules.state.handlers;

import client.modules.state.models.StateRequestProtocolDataUnit;
import client.ship.parents.handlers.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import container.game.docker.modules.state.models.StateResponseProtocolDataUnit;

/**
 * Example handles {@link StateResponseProtocolDataUnit StateResponsePDUs}.
 */
public class StateClientHandler extends ChannelHandler<StateResponseProtocolDataUnit> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StateResponseProtocolDataUnit stateResponsePDU) {
        System.out.printf("State response received from the server %s\n", stateResponsePDU); //todo: log4j
    }

    public void requestState(final Integer x, final Integer y) {
        unicastPDUToServerChannel(new StateRequestProtocolDataUnit(x, y));
    }

    public record GameEntity(Integer x, Integer y) {}
}
