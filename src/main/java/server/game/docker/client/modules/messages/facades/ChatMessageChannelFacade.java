package server.game.docker.client.modules.messages.facades;

import server.game.docker.client.ship.parents.facades.ChannelFacade;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;

public class ChatMessageChannelFacade extends ChannelFacade<ChatMessagePDU> {

    public void receivePlayerLobbyChatMessage(final String playerNickname, final String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendPlayerLobbyChatMessage(final String playerNickname, final String message) {
        unicastPDUToServerChannel(new ChatMessagePDU(playerNickname, message));
    }
}
