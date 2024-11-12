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
        }
    }
}
