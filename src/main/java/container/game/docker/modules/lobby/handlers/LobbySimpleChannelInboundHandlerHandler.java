package container.game.docker.modules.lobby.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import container.game.docker.modules.lobby.facades.LobbyChannelGroupHandler;
import container.game.docker.modules.lobby.pdus.LobbyRequestProtocolDataUnit;

public final class LobbySimpleChannelInboundHandlerHandler extends SimpleChannelInboundHandler<LobbyRequestProtocolDataUnit> {
    private final LobbyChannelGroupHandler lobbyServerFacade;

    public LobbySimpleChannelInboundHandlerHandler(
            final LobbyChannelGroupHandler lobbyServerFacade
    ) {
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, LobbyRequestProtocolDataUnit lobbyReq) {
        final var channel = channelHandlerContext.channel();
        switch (lobbyReq.lobbyRequestFlag()) {
            case 0 -> {
                System.out.printf("Player with ChannelId %s has requested lobby creation\n", channel.id());
                lobbyServerFacade.receiveCreateRequest(channel);
            }
            case 2 -> {
                System.out.printf("Player with ChannelId %s has requested lobby leave\n", channel.id());
                lobbyServerFacade.receiveLeaveRequest(channel);
            }
            case 1 -> {
                System.out.printf("Player with ChannelId %s has requested lobby join\n", channel.id());
                lobbyServerFacade.receiveJoinRequest(channel, lobbyReq.leaderId());
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.printf("A player with ChannelId %s has disconnected, lobby system must take action", ctx.channel().id()); //todo: log4j
        lobbyServerFacade.removeDisconnectedChannelIdFromLobby(ctx.channel().id());
    }
}
