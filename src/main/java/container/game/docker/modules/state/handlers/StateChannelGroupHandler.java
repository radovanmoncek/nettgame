package container.game.docker.modules.state.handlers;

import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import container.game.docker.modules.state.models.StateResponseProtocolDataUnit;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;

/**
 * Example
 */
public class StateChannelGroupHandler extends ChannelGroupHandler<ProtocolDataUnit> {

    public void respondToStateRequest(Integer x, Integer y, Integer x2, Integer y2, ChannelId... players) {
        multicastPDUToClientChannelIds(new StateResponseProtocolDataUnit(x, y, 0, x2, y2), players);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolDataUnit msg) throws Exception {}
}
