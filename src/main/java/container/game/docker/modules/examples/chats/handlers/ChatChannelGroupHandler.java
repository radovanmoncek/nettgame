package container.game.docker.modules.examples.chats.handlers;

import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.examples.models.ExampleNetworkedGamePlayerSessionData;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.PlayerSessionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ChatChannelGroupHandler extends ChannelGroupHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(ChatChannelGroupHandler.class);
    private static final int maxMessageLength = 64;

    @Override
    protected void playerChannelRead(ChatMessageProtocolDataUnit protocolDataUnit, PlayerSessionData playerSession) {
        playerChannelRead(protocolDataUnit, (ExampleNetworkedGamePlayerSessionData) playerSession);
    }

    public void playerChannelRead(final ChatMessageProtocolDataUnit chatMessageProtocolDataUnit, final ExampleNetworkedGamePlayerSessionData playerSession) {

        final var playerChannelIdOptional = playerSession.retrievePlayerChannelId();

        if(playerChannelIdOptional.isEmpty()) {

            return;
        }

        final var playerChannelId = playerChannelIdOptional.get();

        if (chatMessageProtocolDataUnit.message().length() > maxMessageLength) {

            unicastToClientChannel(ChatMessageProtocolDataUnit.newINVALID(), playerChannelId);

            return;
        }

        logger.info(chatMessageProtocolDataUnit);

        playerSession.placeLastChatMessage(chatMessageProtocolDataUnit.message());
    }

    @Override
    protected void playerDisconnected(final PlayerSessionData playerSession) {}
}
