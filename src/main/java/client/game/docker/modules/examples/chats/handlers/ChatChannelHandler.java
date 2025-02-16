package client.game.docker.modules.examples.chats.handlers;

import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Example handler for a chat system.
 */
public class ChatChannelHandler extends ChannelHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(ChatChannelHandler.class);
    private final JPanel client;

    public ChatChannelHandler(final JPanel client) {

        this.client = client;
    }

    @Override
    protected void serverChannelRead(final ChatMessageProtocolDataUnit protocolDataUnit) {

        logger.info("Message from player received {}", protocolDataUnit);
    }
}
