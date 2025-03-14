package container.game.docker.modules.examples.lobbies.handlers;

import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LobbyChannelGroupHandler extends ChannelGroupHandler<LobbyRequestProtocolDataUnit, LobbyResponseProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(LobbyChannelGroupHandler.class);
    private static final String lobbyUUIDProperty = "lobbyUUID";
    private static final String lobbyMember1SessionProperty = "lobbyMember1";
    private static final ConcurrentHashMap<UUID, MultiValueTypeMap> lobbyData = new ConcurrentHashMap<>();

    @Override
    public void playerChannelRead(final LobbyRequestProtocolDataUnit lobbyRequestProtocolDataUnit, final MultiValueTypeMap playerSession) {

        final var playerChannelIdOptional = playerSession.getChannelId(playerChannelIdProperty);

        if(playerChannelIdOptional.isEmpty())
            return;

        final var playerChannelId = playerChannelIdOptional.get();

        switch (lobbyRequestProtocolDataUnit.lobbyFlag()) {

            case CREATE -> {

                logger.info("Player with ChannelId %s has requested lobby creation {}", playerSession.get("playerChannelId"));

                if(playerSession.containsKey(lobbyUUIDProperty)) {

                    unicastToClientChannel(LobbyResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                final var newLobbyUUID = UUID.randomUUID();

                lobbyData.put(newLobbyUUID, MultiValueTypeMap.of(lobbyMember1SessionProperty, playerSession));

                playerSession.put(lobbyUUIDProperty, newLobbyUUID);
            }

            case LEAVE -> {

                logger.info("Player with ChannelId %s has requested lobby leave {}", playerSession.get("playerChannelId"));

                if(!playerSession.containsKey(lobbyUUIDProperty)) {

                    unicastToClientChannel(LobbyResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                final var lobbyUUIDOptional = playerSession.getUUID(lobbyUUIDProperty);

                if(lobbyUUIDOptional.isEmpty()){

                    logger.error("A player had empty lobbyUUIDProperty in their playerSession, this should never occur");

                    return;
                }

                playerSession.remove(lobbyUUIDProperty);
            }

            case JOIN -> {

                logger.info("Player with ChannelId %s has requested lobby join {}", playerSession.get("playerChannelId"));


            }
        }
    }

    @Override
    public void playerDisconnected(final MultiValueTypeMap playerSession) {}
}
