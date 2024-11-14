package server.game.docker.modules.lobby.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;

public final class LobbyServerHandler extends SimpleChannelInboundHandler<LobbyRequestPDU> {
    private final LobbyServerFacade lobbyServerFacade;

    public LobbyServerHandler(
            LobbyServerFacade lobbyServerFacade
    ) {
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, LobbyRequestPDU lobbyReq) {
        final var channel = channelHandlerContext.channel();
        switch (lobbyReq.getActionFlag()) {
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
                lobbyServerFacade.receiveJoinRequest(channel, lobbyReq.getLeaderId());
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.printf("A player with ChannelId %s has disconnected, lobby system must take action", ctx.channel().id());
        lobbyServerFacade.removeDisconnectedChannelIdFromLobby(ctx.channel().id());
    }
}
