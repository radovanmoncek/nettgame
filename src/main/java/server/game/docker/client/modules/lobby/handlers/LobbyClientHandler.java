package server.game.docker.client.modules.lobby.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.modules.lobby.pdus.LobbyResponsePDU;

public class LobbyClientHandler extends SimpleChannelInboundHandler<LobbyResponsePDU> {
    private final LobbyClientFacade lobbyClientFacade;

    public LobbyClientHandler(final LobbyClientFacade lobbyClientFacade) {
        this.lobbyClientFacade = lobbyClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LobbyResponsePDU lobbyUpdate) {
        switch (lobbyUpdate.lobbyUpdateResponseFlag()) {
            case 0 -> {
                System.out.printf("Received lobby create response %s from the server\n", lobbyUpdate); // todo: log4j
                lobbyClientFacade.receiveLobbyCreated(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 2 -> {
                System.out.printf("Received lobby left response %s from the server\n", lobbyUpdate); //todo: log4j
                lobbyClientFacade.receiveLobbyLeft(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 1 -> {
                System.out.printf("Received lobby join response %s from the server\n", lobbyUpdate); //todo: log4j
                lobbyClientFacade.receiveLobbyJoined(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
        }
    }
}
