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

    public void receiveCreateRequest(final Channel clientChannel) {
        if(lobbyMemberships.containsKey(clientChannel.id()))
            return;
        for (final LinkedList<ChannelId> lobby : lobbyMembers){
            if(lobby.contains(clientChannel.id()))
                return;
        }
        lobbyMembers.add(new LinkedList<>(List.of(clientChannel.id())));
        lobbyMemberships.computeIfAbsent(clientChannel.id(), ignored -> lobbyMembers.size() - 1);
        final var lobbyUpdate = new LobbyUpdatePDU();
        lobbyUpdate.setStateFlag(LobbyUpdatePDU.CREATED);
        lobbyUpdate.setLeaderId(Long.parseLong(clientChannel.id().asShortText()));
        lobbyUpdate.setMembers(lobbyMembers.get(lobbyMembers.size() - 1).stream().map(playerServerFacade::getClientUsername).toList());
        unicastPDUToClientChannel(lobbyUpdate, clientChannel);
    }

    public void receiveJoinRequest(final ChannelId clientChannelId){
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    public void receiveLeaveRequest(final ChannelId clientChannelId){
        throw new UnsupportedOperationException("Method is not implemented.");
    }
}
