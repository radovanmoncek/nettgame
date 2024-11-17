package server.game.docker.client.modules.state.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.state.facades.StateClientFacade;
import server.game.docker.modules.state.pdus.StateResponsePDU;

/**
 * Example handles {@link StateResponsePDU StateResponsePDUs}.
 */
public class StateClientHandler extends SimpleChannelInboundHandler<StateResponsePDU> {
    private final StateClientFacade stateClientFacade;

    public StateClientHandler(StateClientFacade stateClientFacade) {
        this.stateClientFacade = stateClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StateResponsePDU msg) {
        System.out.printf("State response received from the server %s\n", msg);
        stateClientFacade.receiveState(msg.playerNickname(), msg.x(), msg.y());
    }
}
