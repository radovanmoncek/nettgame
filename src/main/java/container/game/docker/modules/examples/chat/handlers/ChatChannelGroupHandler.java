package container.game.docker.modules.examples.chat.handlers;

import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import io.netty.channel.ChannelId;

public final class ChatChannelGroupHandler extends ChannelGroupHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    public static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void playerChannelRead(final ChatMessageProtocolDataUnit chatMessageProtocolDataUnit, final ChannelId channelId) {

        System.out.println(chatMessageProtocolDataUnit); //todo: log4j
    }

    @Override
    protected void playerDisconnected(ChannelId id) {

    }
}
