package container.game.docker.modules.examples.lobby.handlers;

import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;

public final class LobbyChannelGroupHandler extends ChannelGroupHandler<LobbyRequestProtocolDataUnit, LobbyResponseProtocolDataUnit> {

    @Override
    public void playerChannelRead(final LobbyRequestProtocolDataUnit lobbyRequestProtocolDataUnit, final MultiValueTypeMap playerSession) {

        switch (lobbyRequestProtocolDataUnit.lobbyFlag()) {

            case CREATE -> {

                System.out.printf("Player with ChannelId %s has requested lobby creation\n", playerSession.get("playerChannelId")); //todo: log4j


            }

            case LEAVE -> {

                System.out.printf("Player with ChannelId %s has requested lobby leave\n", playerSession.get("playerChannelId")); //todo: log4j


            }

            case JOIN -> {

                System.out.printf("Player with ChannelId %s has requested lobby join\n", playerSession.get("playerChannelId")); //todo: log4j


            }
        }
    }

    @Override
    public void playerDisconnected(final MultiValueTypeMap playerSession) {

        /*final var lobby = lobbyMemberships.get(lobbyHash);

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

        lobbyMemberships.put(lobbyHash, new Lobby(lobby.member1, null));*/
    }
}
