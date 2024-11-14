package server.game.docker.modules.lobby.facades;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import server.game.docker.modules.lobby.pdus.LobbyUpdatePDU;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.ship.parents.facades.ServerFacade;

import java.util.*;

public class LobbyServerFacade extends ServerFacade<LobbyUpdatePDU> {
    private final Map<ChannelId, Integer> lobbyMemberships;
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
        /*for (final LinkedList<ChannelId> lobby : lobbyMembers){
            if(lobby.contains(clientChannel.id()))
                return;
        }*/
        lobbyMembers.add(new LinkedList<>(List.of(clientChannel.id())));
        lobbyMemberships.computeIfAbsent(clientChannel.id(), ignored -> lobbyMembers.size() - 1);
        final var lobbyUpdate = new LobbyUpdatePDU();
        lobbyUpdate.setStateFlag(LobbyUpdatePDU.CREATED);
        lobbyUpdate.setLeaderId(Long.parseLong(clientChannel.id().asShortText(), 16));
        lobbyUpdate.setMembers(lobbyMembers.get(lobbyMembers.size() - 1).stream().map(playerServerFacade::getNickname).toList());
        unicastPDUToClientChannel(lobbyUpdate, clientChannel);
    }

    public synchronized void receiveJoinRequest(final Channel clientChannel, final Long lobbyLeaderId) {
        if (lobbyMemberships.containsKey(clientChannel.id()))
            return;

        lobbyMemberships.entrySet().stream().filter(entry -> lobbyLeaderId.equals(Long.valueOf(entry.getKey().asShortText(), 16))).findAny().ifPresent(entry -> {
            final var lobby = lobbyMembers.get(entry.getValue());
            if(lobby.size() > 2)
                throw new RuntimeException(String.format("Lobby %s with more than 2 members exists.\n", lobby));
            lobbyMembers.get(entry.getValue()).add(clientChannel.id());

            final var lobbyUpdate = new LobbyUpdatePDU();
            lobbyUpdate.setStateFlag(LobbyUpdatePDU.JOINED);
            lobbyUpdate.setLeaderId(lobbyLeaderId);
            lobbyUpdate.setMembers(lobby.stream().map(playerServerFacade::getNickname).toList());
            unicastPDUToClientChannel(lobbyUpdate, clientChannel);
        });
    }

    public synchronized void receiveLeaveRequest(final Channel clientChannel) {
        final var lobbyUpdate = new LobbyUpdatePDU();

        if (!lobbyMemberships.containsKey(clientChannel.id()))
            return;

        final var lobby = lobbyMembers.get(lobbyMemberships.get(clientChannel.id()));

        if (lobby.isEmpty()) {
            throw new RuntimeException(String.format("Lobby %s with size 0 exists.\n", lobby));
        }

        if (lobby.size() == 1) {
            lobbyMembers.remove((int) lobbyMemberships.remove(clientChannel.id()));

            lobbyUpdate.setStateFlag(LobbyUpdatePDU.LEFT);
        }

        if (lobby.size() == 2) {
            lobbyMemberships.remove(clientChannel.id());
            lobby.remove(clientChannel.id());

            lobbyUpdate.setStateFlag(LobbyUpdatePDU.MEMBERLEFT);
        }

        lobbyUpdate.setLeaderId(-1L);
        lobbyUpdate.setMembers(Collections.emptyList());
        unicastPDUToClientChannel(lobbyUpdate, clientChannel);
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
}
