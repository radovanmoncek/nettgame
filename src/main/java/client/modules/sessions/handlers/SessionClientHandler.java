package client.modules.sessions.handlers;

import client.ship.parents.handlers.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import container.game.docker.modules.session.models.SessionProtocolDataUnit;

public class SessionClientHandler extends ChannelHandler<SessionProtocolDataUnit> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SessionProtocolDataUnit msg) {}

    public void requestStartSession() {
        unicastPDUToServerChannel(new SessionProtocolDataUnit((byte) SessionProtocolDataUnit.SessionFlag.START.ordinal()));
    }

    public void requestStopSession() {
        unicastPDUToServerChannel(new SessionProtocolDataUnit((byte) SessionProtocolDataUnit.SessionFlag.STOP.ordinal()));
    }
}
