package client.game.docker.modules.examples.chats.handlers;

import client.game.docker.ship.parents.handlers.ServerChannelHandler;
import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import container.game.docker.modules.examples.chats.models.ChatMessageFlatBufferSerializable;
import container.game.docker.ship.examples.compiled.schemas.ChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Example handler for a chat system.
 */
public class ChatServerChannelHandler extends ServerChannelHandler<ChatMessage> {
    private static final Logger logger = LogManager.getLogger(ChatServerChannelHandler.class);

    public ChatServerChannelHandler() {}

    @Override
    protected void serverChannelRead(final ChatMessage message) {

        logger.info("Message from player received {}", message);
    }
}
