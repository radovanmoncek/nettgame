package cz.radovanmoncek.server.modules.chats.handlers;

import cz.radovanmoncek.server.modules.chats.models.ChatMessageFlatBufferSerializable;
import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.ship.parents.handlers.ChannelGroupHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ExampleGameChatHandler extends ChannelGroupHandler<ChatMessage> {
    private static final Logger logger = LogManager.getLogger(ExampleGameChatHandler.class);
    private static final int maxMessageLength = 64;
    private static final AttributeKey<ConcurrentLinkedQueue<ChatMessage>> chatMessageQueueAttribute = AttributeKey.valueOf("chatMessageQueue");

    public void channelRead0(final ChannelHandlerContext channelHandlerContext, final ChatMessage chatMessage) {

        final var playerChannel = channelHandlerContext.channel();

        if (Objects.requireNonNullElse(chatMessage.messageContent(), "").length() > maxMessageLength) {

            playerChannel.writeAndFlush(new ChatMessageFlatBufferSerializable("system", "invalid message"));

            return;
        }

        logger.info(chatMessage);

        final var chatMessageQueue = playerChannel
                .attr(chatMessageQueueAttribute)
                .get();

        if(Objects.isNull(chatMessageQueue)) {

            return;
        }

        chatMessageQueue.offer(chatMessage);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) {

        super.handlerAdded(channelHandlerContext);

        channelHandlerContext
                .channel()
                .attr(chatMessageQueueAttribute)
                .set(new ConcurrentLinkedQueue<>());
    }
}
