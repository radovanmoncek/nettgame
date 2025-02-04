package server.game.docker.modules.player.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.player.facades.PlayerChannelGroupHandler;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;

public final class PlayerServerHandler extends SimpleChannelInboundHandler<NicknameProtocolDataUnit> {
    private final PlayerChannelGroupHandler playerServerFacade;

    public PlayerServerHandler(
            final PlayerChannelGroupHandler playerServerFacade
    ) {
        this.playerServerFacade = playerServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, NicknameProtocolDataUnit nicknamePDU) {
        System.out.printf("A client with ChannelId %s has requested a new username: %s\n", ctx.channel().id(), nicknamePDU.nickname()); //todo: log4j
        playerServerFacade.receiveNicknameRequest(nicknamePDU.nickname(), ctx.channel());
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
