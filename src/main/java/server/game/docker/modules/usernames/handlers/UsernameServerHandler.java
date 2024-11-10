package server.game.docker.modules.usernames.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.usernames.facades.UsernameServerFacade;
import server.game.docker.modules.usernames.pdus.UsernamePDU;

public final class UsernameServerHandler extends SimpleChannelInboundHandler<UsernamePDU> {
    private final UsernameServerFacade usernameServerFacade;

    public UsernameServerHandler(
            final UsernameServerFacade usernameServerFacade
    ) {
        this.usernameServerFacade = usernameServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UsernamePDU usernamePDU) {
        System.out.printf("A client with ChannelId %s has requested a new username: %s\n", ctx.channel().id(), usernamePDU.getNewClientUsername()); //todo: log4j
        usernameServerFacade.assignClientUsername(usernamePDU.getNewClientUsername(), ctx.channel());
        usernameServerFacade.receiveClientUsernameRequest();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.printf("A client has connected with channel id(): %s\n", ctx.channel().id()); //todo: log4j
        usernameServerFacade.startManagingClient(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.printf("Client with ChannelId %s has disconnected\n", ctx.channel().id()); //todo: log4j
        usernameServerFacade.removeClientUsername(ctx.channel().id());
        usernameServerFacade.stopManagingClient(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
    }
}
