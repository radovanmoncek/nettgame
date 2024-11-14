package server.game.docker.modules.player.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.player.pdus.NicknamePDU;

public final class PlayerServerHandler extends SimpleChannelInboundHandler<NicknamePDU> {
    private final PlayerServerFacade playerServerFacade;

    public PlayerServerHandler(
            final PlayerServerFacade playerServerFacade
    ) {
        this.playerServerFacade = playerServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, NicknamePDU nicknamePDU) {
        System.out.printf("A client with ChannelId %s has requested a new username: %s\n", ctx.channel().id(), nicknamePDU.getNewNickname()); //todo: log4j
        playerServerFacade.receiveNicknameRequest(nicknamePDU.getNewNickname(), ctx.channel());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.printf("A client has connected with channel id(): %s\n", ctx.channel().id()); //todo: log4j
        playerServerFacade.startManagingClient(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.printf("Client with ChannelId %s has disconnected\n", ctx.channel().id()); //todo: log4j
        playerServerFacade.removeNickname(ctx.channel().id());
        playerServerFacade.stopManagingClient(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println(cause.getMessage());
    }
}
