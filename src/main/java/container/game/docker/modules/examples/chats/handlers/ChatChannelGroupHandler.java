package container.game.docker.modules.examples.chats.handlers;

import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ChatChannelGroupHandler extends ChannelGroupHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(ChatChannelGroupHandler.class);
    private static final int maxMessageLength = 64;

    @Override
    public void playerChannelRead(final ChatMessageProtocolDataUnit chatMessageProtocolDataUnit, final MultiValueTypeMap playerSession) {

        final var playerChannelIdOptional = playerSession.getChannelId(ChannelGroupHandler.playerChannelIdProperty);

        if(playerChannelIdOptional.isEmpty()) {

            return;
        }

        final var playerChannelId = playerChannelIdOptional.get();

        if (chatMessageProtocolDataUnit.message().length() > maxMessageLength) {

            unicastToClientChannel(ChatMessageProtocolDataUnit.newINVALID(), playerChannelId);

            return;
        }

        logger.info(chatMessageProtocolDataUnit);
    }

    @Override
    protected void playerDisconnected(final MultiValueTypeMap playerSession) {}
}
