package container.game.docker.modules.examples.chat.handlers;

import container.game.docker.modules.examples.chat.models.ChatMessageProtocolDataUnit;
import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;

public final class ChatChannelGroupHandler extends ChannelGroupHandler<ChatMessageProtocolDataUnit, ChatMessageProtocolDataUnit> {
    public static final int MAX_MESSAGE_LENGTH = 64;

    @Override
    public void playerChannelRead(final ChatMessageProtocolDataUnit chatMessageProtocolDataUnit, final MultiValueTypeMap playerSession) {

        System.out.println(chatMessageProtocolDataUnit); //todo: log4j
    }

    @Override
    protected void playerDisconnected(final MultiValueTypeMap playerSession) {
    }
}
