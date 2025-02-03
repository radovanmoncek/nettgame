package client.modules.state.handlers;

import client.modules.state.models.StateRequestPDU;
import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.state.pdus.StateResponsePDU;

import java.util.List;

/**
 * Example handles {@link StateResponsePDU StateResponsePDUs}.
 */
public class StateClientHandler extends ChannelPDUCommunicationsHandler<StateResponsePDU> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StateResponsePDU stateResponsePDU) {
        System.out.printf("State response received from the server %s\n", stateResponsePDU);
        receiveState(List.of(new GameEntity(stateResponsePDU.x(), stateResponsePDU.y()), new GameEntity(stateResponsePDU.x2(), stateResponsePDU.y2())));
    }

    public void requestState(final Integer x, final Integer y) {
        unicastPDUToServerChannel(new StateRequestPDU(x, y));
    }

    public void receiveState(final List<GameEntity> gameEntities) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public record GameEntity(Integer x, Integer y) {}
}
