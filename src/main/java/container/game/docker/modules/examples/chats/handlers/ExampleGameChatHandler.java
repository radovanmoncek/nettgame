package container.game.docker.modules.examples.chats.handlers;

import container.game.docker.ship.examples.compiled.schemas.ChatMessage;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ExampleGameChatHandler extends ChannelGroupHandler<ChatMessage> {
    private static final Logger logger = LogManager.getLogger(ExampleGameChatHandler.class);
    private static final int maxMessageLength = 64;
    private static final AttributeKey<String> latestChatMessage = AttributeKey.valueOf("latestChatMessage");

    public void playerChannelRead(final ChatMessage chatMessage, final Channel playerChannel) {

        if (chatMessage.messageContent().length() > maxMessageLength) {

            playerChannel.writeAndFlush(-1);

            return;
        }

        logger.info(chatMessage);

        playerChannel
                .attr(latestChatMessage)
                .set(chatMessage.messageContent());
    }

    @Override
    protected void playerDisconnected(final Channel playerChannel) {}
}
