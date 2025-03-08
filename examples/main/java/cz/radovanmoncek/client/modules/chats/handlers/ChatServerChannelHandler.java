package cz.radovanmoncek.client.modules.chats.handlers;

import cz.radovanmoncek.client.ship.parents.handlers.ServerChannelHandler;
import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Example handler for a chat system.
 */
public class ChatServerChannelHandler extends ServerChannelHandler<ChatMessage> {
    private static final Logger logger = LogManager.getLogger(ChatServerChannelHandler.class);

    public ChatServerChannelHandler() {}

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final ChatMessage chatMessage) {

        logger.info("Message from player received {}", chatMessage);
    }
}
