package client.modules.messages.handlers;

import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelHandlerContext;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

public class ChatMessageClientHandler extends ChannelPDUCommunicationsHandler<ChatMessagePDU> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessagePDU msg) {
        System.out.printf("Player lobby message received %s\n", msg);
        receivePlayerLobbyChatMessage(msg.authorNick(), msg.message());
    }

    /**
     * This method is called when a new chat message is received from the server.
     * @param playerNickname
     * @param message
     */
    public void receivePlayerLobbyChatMessage(final String playerNickname, final String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendPlayerLobbyChatMessage(final String playerNickname, final String message) {
        unicastPDUToServerChannel(new ChatMessagePDU(playerNickname, message));
    }
}
