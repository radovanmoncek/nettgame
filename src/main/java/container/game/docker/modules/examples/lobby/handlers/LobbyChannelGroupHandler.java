package container.game.docker.modules.examples.lobby.handlers;

import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LobbyChannelGroupHandler extends ChannelGroupHandler<LobbyRequestProtocolDataUnit, LobbyResponseProtocolDataUnit> {
    private static final ConcurrentHashMap<Integer, Lobby> lobbyMemberships = new ConcurrentHashMap<>();
    private int lobbyHash;

    @Override
    public void playerChannelRead(final LobbyRequestProtocolDataUnit lobbyRequestProtocolDataUnit, final ChannelId channelId) {

        switch (lobbyRequestProtocolDataUnit.lobbyFlag()) {

            case CREATE -> {

                System.out.printf("Player with ChannelId %s has requested lobby creation\n", channelId); //todo: log4j


            }

            case LEAVE -> {

                System.out.printf("Player with ChannelId %s has requested lobby leave\n", channelId); //todo: log4j


            }

            case JOIN -> {

                System.out.printf("Player with ChannelId %s has requested lobby join\n", channelId); //todo: log4j


            }
        }
    }

    @Override
    public void playerDisconnected(final ChannelId playerChannelId) {

        final var lobby = lobbyMemberships.get(lobbyHash);

        if(lobby == null)
            return;

        if(lobby.member1 == null && lobby.member2 == null){

            lobbyMemberships.remove(lobbyHash);

            return;
        }

        if(Objects.requireNonNull(lobby.member1).channelId().equals(playerChannelId)) {

            lobbyMemberships.put(lobbyHash, new Lobby(null, lobby.member2));

            return;
        }

        lobbyMemberships.put(lobbyHash, new Lobby(lobby.member1, null));
    }

    private record Lobby(Member member1, Member member2) {}

    private record Member(ChannelId channelId, String nickname){}
}
