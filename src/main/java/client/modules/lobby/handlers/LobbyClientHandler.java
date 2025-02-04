package client.modules.lobby.handlers;

import client.ship.parents.handlers.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import container.game.docker.modules.lobby.pdus.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.lobby.pdus.LobbyResponseProtocolDataUnit;

import java.util.Collection;

public class LobbyClientHandler extends ChannelHandler<LobbyResponseProtocolDataUnit> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LobbyResponseProtocolDataUnit lobbyUpdate) {}

    /**
     * <p>
     * Sends a {@link LobbyRequestProtocolDataUnit} to the server with the actionFlag attribute set to 0 (CREATE).
     * </p>
     */
    public void createLobby() {
        final var lobbyRequest = new LobbyRequestProtocolDataUnit((byte) LobbyRequestProtocolDataUnit.LobbyRequestFlag.CREATE.ordinal(), null);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void joinLobby(final Long leaderChannelId) {
        final var lobbyReq = new LobbyRequestProtocolDataUnit((byte) LobbyRequestProtocolDataUnit.LobbyRequestFlag.JOIN.ordinal(), leaderChannelId);
        unicastPDUToServerChannel(lobbyReq);
    }

    public void leaveLobby() {
        final var lobbyRequest = new LobbyRequestProtocolDataUnit((byte) LobbyRequestProtocolDataUnit.LobbyRequestFlag.LEAVE.ordinal(), null);
        unicastPDUToServerChannel(lobbyRequest);
    }
}
