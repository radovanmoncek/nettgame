package server.game.docker.client.modules.sessions.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.sessions.facades.SessionChannelFacade;
import server.game.docker.modules.session.pdus.SessionPDU;

public class SessionClientHandler extends SimpleChannelInboundHandler<SessionPDU> {
    private final SessionChannelFacade sessionClientFacade;

    public SessionClientHandler(SessionChannelFacade sessionClientFacade) {
        this.sessionClientFacade = sessionClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SessionPDU msg) {
        switch (msg.sessionFlag()){
            case 0 -> {
                System.out.printf("%s\n", msg);
                sessionClientFacade.receiveStartSessionResponse();
            }
            case 2 ->{
                System.out.printf("%s\n", msg);
                sessionClientFacade.receiveStopSessionResponse();
            }
        }
    }
}
