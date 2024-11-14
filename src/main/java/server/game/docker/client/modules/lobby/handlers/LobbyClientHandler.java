package server.game.docker.client.modules.lobby.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.modules.lobby.pdus.LobbyUpdatePDU;

public class LobbyClientHandler extends SimpleChannelInboundHandler<LobbyUpdatePDU> {
    private final LobbyClientFacade lobbyClientFacade;

    public LobbyClientHandler(final LobbyClientFacade lobbyClientFacade) {
        this.lobbyClientFacade = lobbyClientFacade;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LobbyUpdatePDU lobbyUpdate) {
        switch (lobbyUpdate.getStateFlag()) {
            case 0 -> lobbyClientFacade.receiveLobbyCreated(lobbyUpdate.getLeaderId(), lobbyUpdate.getMembers());
            case 2 -> {
                System.out.printf("Received lobby left response %s from the server\n", lobbyUpdate); //todo: log4j
                lobbyClientFacade.receiveLobbyLeft(lobbyUpdate.getLeaderId(), lobbyUpdate.getMembers());
            }
            case 1 -> {
                System.out.printf("Received lobby join response %s from the server\n", lobbyUpdate); //todo: log4j
                lobbyClientFacade.receiveLobbyJoined(lobbyUpdate.getLeaderId(), lobbyUpdate.getMembers());
            }
        }
    }
}
