package client.modules.lobby.handlers;

import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.lobby.pdus.LobbyRequestPDU;
import server.game.docker.modules.lobby.pdus.LobbyResponsePDU;

import java.util.Collection;

public class LobbyClientHandler extends ChannelPDUCommunicationsHandler<LobbyResponsePDU> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LobbyResponsePDU lobbyUpdate) {
        switch (lobbyUpdate.lobbyUpdateResponseFlag()) {
            case 0 -> {
                System.out.printf("Received lobby create response %s from the server\n", lobbyUpdate); // todo: log4j
                receiveLobbyCreated(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 2 -> {
                System.out.printf("Received lobby left response %s from the server\n", lobbyUpdate); //todo: log4j
                receiveLobbyLeft(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 1 -> {
                System.out.printf("Received lobby join response %s from the server\n", lobbyUpdate); //todo: log4j
                receiveLobbyJoined(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 3 -> {
                System.out.printf("Received lobby member join response %s from the server\n", lobbyUpdate); //todo: log4j
                receiveLobbyMemberJoined(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
            case 4 -> {
                System.out.printf("Received lobby member leave response %s from the server\n", lobbyUpdate); //todo: log4j
                receiveLobbyMemberLeft(lobbyUpdate.leaderId(), lobbyUpdate.members());
            }
        }
    }

    /**
     * <p>
     * Sends a {@link LobbyRequestPDU} to the server with the actionFlag attribute set to 0 (CREATE).
     * </p>
     */
    public void createLobby() {
        final var lobbyRequest = new LobbyRequestPDU((byte) LobbyRequestPDU.LobbyRequestFlag.CREATE.ordinal(), null);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void joinLobby(final Long leaderChannelId) {
        final var lobbyReq = new LobbyRequestPDU((byte) LobbyRequestPDU.LobbyRequestFlag.JOIN.ordinal(), leaderChannelId);
        unicastPDUToServerChannel(lobbyReq);
    }

    public void leaveLobby() {
        final var lobbyRequest = new LobbyRequestPDU((byte) LobbyRequestPDU.LobbyRequestFlag.LEAVE.ordinal(), null);
        unicastPDUToServerChannel(lobbyRequest);
    }

    public void receiveLobbyCreated(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyCreated is not implemented.");
    }

    public void receiveLobbyLeft(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyLeft is not implemented.");
    }

    public void receiveLobbyJoined(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyJoined is not implemented.");
    }

    public void receiveLobbyMemberJoined(final Long aLong, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyMemberJoined is not implemented.");
    }

    public void receiveLobbyMemberLeft(final Long leaderId, final Collection<String> members) {
        throw new UnsupportedOperationException("Method receiveLobbyMemberLeft is not implemented.");
    }
}
