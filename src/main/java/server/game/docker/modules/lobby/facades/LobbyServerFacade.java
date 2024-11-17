package server.game.docker.modules.lobby.facades;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import server.game.docker.modules.lobby.pdus.LobbyResponsePDU;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.ship.parents.facades.ServerFacade;

import java.util.*;

public class LobbyServerFacade extends ServerFacade<LobbyResponsePDU> {
    private final Map<ChannelId, Integer> lobbyMemberships;
    @Deprecated
    private final ArrayList<LinkedList<ChannelId>> lobbyMembers;
    private final PlayerServerFacade playerServerFacade;

    public LobbyServerFacade(PlayerServerFacade playerServerFacade) {
        this.playerServerFacade = playerServerFacade;
        lobbyMemberships = new HashMap<>();
        lobbyMembers = new ArrayList<>();
    }

    public synchronized void receiveCreateRequest(final Channel clientChannel) {
        if (lobbyMemberships.containsKey(clientChannel.id()))
            return;
        lobbyMembers.add(new LinkedList<>(List.of(clientChannel.id())));
        lobbyMemberships.computeIfAbsent(clientChannel.id(), ignored -> lobbyMembers.size() - 1);
        final var lobbyUpdate = new LobbyResponsePDU(
                (byte) LobbyResponsePDU.LobbyUpdateResponseFlag.CREATED.ordinal(),
                Long.parseLong(clientChannel.id().asShortText(), 16),
                lobbyMembers.get(lobbyMembers.size() - 1).stream().map(playerServerFacade::getNickname).toList()
        );
        unicastPDUToClientChannel(lobbyUpdate, clientChannel);
    }

    public synchronized void receiveJoinRequest(final Channel clientChannel, final Long lobbyLeaderId) {
        if (lobbyMemberships.containsKey(clientChannel.id()))
            return;

        lobbyMemberships
                .entrySet()
                .stream()
                .filter(entry -> lobbyLeaderId.equals(Long.valueOf(entry.getKey().asShortText(), 16)) && lobbyMembers.get(entry.getValue()).size() < 2)
                .findAny()
                .ifPresent(entry -> {
            final var lobby = lobbyMembers.get(entry.getValue());

            if(lobby.size() > 2)
                throw new RuntimeException(String.format("Lobby %s with more than 2 members exists.\n", lobby));

            lobbyMembers.get(entry.getValue()).add(clientChannel.id());
            lobbyMemberships.computeIfAbsent(clientChannel.id(), ignored -> lobbyMembers.size() - 1);

            final var lobbyUpdate = new LobbyResponsePDU(
                    (byte) LobbyResponsePDU.LobbyUpdateResponseFlag.JOINED.ordinal(),
                    lobbyLeaderId, lobby.stream().map(playerServerFacade::getNickname).toList()
            );

            unicastPDUToClientChannel(lobbyUpdate, clientChannel);
        });
    }

    public synchronized void receiveLeaveRequest(final Channel clientChannel) {
        if (!lobbyMemberships.containsKey(clientChannel.id()))
            return;

        final var lobby = lobbyMembers.get(lobbyMemberships.get(clientChannel.id()));

        if (lobby.isEmpty()) {
            throw new RuntimeException(String.format("Lobby %s with size 0 exists.\n", lobby));
        }

        if (lobby.size() == 1) {
            lobbyMembers.remove((int) lobbyMemberships.remove(clientChannel.id()));
        }

        if (lobby.size() == 2) {
            lobbyMemberships.remove(clientChannel.id());
            lobby.remove(clientChannel.id());

            final var lobbyUpdateForOtherMembers = new LobbyResponsePDU(
                    (byte) LobbyResponsePDU.LobbyUpdateResponseFlag.MEMBERLEFT.ordinal(),
                    Long.valueOf(lobby.get(0).asShortText(), 16),
                    lobby.stream().map(playerServerFacade::getNickname).toList()
            );

            multicastPDUToClientChannelIds(lobbyUpdateForOtherMembers, lobby.get(0));
        }

        final var lobbyUpdateForLeavingPlayer = new LobbyResponsePDU((byte) LobbyResponsePDU.LobbyUpdateResponseFlag.LEFT.ordinal(), -1L, Collections.emptyList());
        unicastPDUToClientChannel(lobbyUpdateForLeavingPlayer, clientChannel);
    }

    public void removeDisconnectedChannelIdFromLobby(final ChannelId playerChannelId) {
        if (!lobbyMemberships.containsKey(playerChannelId))
            return;

        final var lobby = lobbyMembers.get(lobbyMemberships.get(playerChannelId));

        if (lobby.isEmpty()) {
            throw new RuntimeException(String.format("Lobby %s with size 0 exists.\n", lobby));
        }

        if (lobby.size() == 1) {
            lobbyMembers.remove((int) lobbyMemberships.remove(playerChannelId));
        }

        if (lobby.size() == 2) {
            lobbyMemberships.remove(playerChannelId);
            lobby.remove(playerChannelId);
        }
    }

    public LinkedList<ChannelId> findPlayerLobby(final ChannelId playerChannelId) {
        return lobbyMembers.get(lobbyMemberships.get(playerChannelId)); //todo: return Optional
    }
}
