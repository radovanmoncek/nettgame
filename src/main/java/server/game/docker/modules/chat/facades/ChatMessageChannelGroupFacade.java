package server.game.docker.modules.chat.facades;

import io.netty.channel.ChannelId;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;
import server.game.docker.ship.parents.facades.ChannelGroupFacade;

public class ChatMessageChannelGroupFacade extends ChannelGroupFacade<ChatMessagePDU> {

    public void receivePlayerLobbyMessage(final String playerNickname, final String playerMessage, final ChannelId receiverId) {
        if(receiverId != null) {
            multicastPDUToClientChannelIds(new ChatMessagePDU(playerNickname, playerMessage), receiverId);
        }
    }

    public void sendPlayerLobbyMessage(final String playerNickname, final String playerMessage){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}