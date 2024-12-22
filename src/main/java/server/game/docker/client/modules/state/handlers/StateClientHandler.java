package server.game.docker.client.modules.state.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.state.facades.StateChannelFacade;
import server.game.docker.modules.state.pdus.StateResponsePDU;

import java.util.List;

/**
 * Example handles {@link StateResponsePDU StateResponsePDUs}.
 */
public class StateClientHandler extends SimpleChannelInboundHandler<StateResponsePDU> {
    private final StateChannelFacade stateClientFacade;

    public StateClientHandler(StateChannelFacade stateClientFacade) {
        this.stateClientFacade = stateClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StateResponsePDU stateResponsePDU) {
        System.out.printf("State response received from the server %s\n", stateResponsePDU);
        stateClientFacade.receiveState(List.of(new StateChannelFacade.GameEntity(stateResponsePDU.x(), stateResponsePDU.y()), new StateChannelFacade.GameEntity(stateResponsePDU.x2(), stateResponsePDU.y2())));
    }
}
