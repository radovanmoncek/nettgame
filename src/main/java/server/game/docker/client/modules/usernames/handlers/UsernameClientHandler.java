package server.game.docker.client.modules.usernames.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.usernames.facades.UsernameClientFacade;
import server.game.docker.modules.usernames.pdus.UsernamePDU;

public class UsernameClientHandler extends SimpleChannelInboundHandler<UsernamePDU> {
    private final UsernameClientFacade usernameClientFacade;

    public UsernameClientHandler(final UsernameClientFacade usernameClientFacade) {
        this.usernameClientFacade = usernameClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UsernamePDU msg) {
        usernameClientFacade.receiveNewUsername(msg.getNewClientUsername());
    }
}
