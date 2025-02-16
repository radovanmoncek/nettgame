package client.game.docker.modules.examples.chats.handlers;

import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;

import javax.swing.*;

/**
 * Example handler for a chat system.
 */
public class ChatChannelHandler extends ChannelHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    private final JPanel client;

    public ChatChannelHandler(JPanel client) {

        this.client = client;
    }

    @Override
    protected void serverChannelRead(final ChatMessageProtocolDataUnit protocolDataUnit) {

        System.out.printf("Message from player received %s\n", protocolDataUnit); // todo: log4j
    }
}
