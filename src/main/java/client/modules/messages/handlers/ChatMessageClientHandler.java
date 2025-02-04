package client.modules.messages.handlers;

import client.ship.parents.handlers.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import container.game.docker.modules.chat.examples.models.ChatMessageProtocolDataUnit;

public class ChatMessageClientHandler extends ChannelHandler<ChatMessageProtocolDataUnit> {

    /**
     * Called when a chat message is received.
     * @param ctx           the {@link ChannelHandlerContext} which this {@link io.netty.channel.SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessageProtocolDataUnit msg) {
        System.out.printf("Player lobby message received %s\n", msg); // todo: log4j
    }

    public void sendPlayerLobbyChatMessage(final String playerNickname, final String message) {
        unicastPDUToServerChannel(new ChatMessageProtocolDataUnit(playerNickname, message));
    }
}
