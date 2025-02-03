package client.modules.sessions.handlers;

import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.session.pdus.SessionPDU;

public class SessionClientHandler extends ChannelPDUCommunicationsHandler<SessionPDU> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SessionPDU msg) {}

    public void requestStartSession() {
        unicastPDUToServerChannel(new SessionPDU((byte) SessionPDU.SessionFlag.START.ordinal()));
    }

    public void requestStopSession() {
        unicastPDUToServerChannel(new SessionPDU((byte) SessionPDU.SessionFlag.STOP.ordinal()));
    }
}
