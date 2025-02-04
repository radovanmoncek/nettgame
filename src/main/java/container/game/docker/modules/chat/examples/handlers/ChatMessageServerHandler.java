package container.game.docker.modules.chat.examples.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import container.game.docker.modules.chat.handlers.ChatMessageChannelGroupHandler;
import container.game.docker.modules.chat.modules.ChatMessageProtocolDataUnit;
import container.game.docker.modules.lobby.facades.LobbyChannelGroupHandler;

public final class ChatMessageServerHandler extends SimpleChannelInboundHandler<ChatMessageProtocolDataUnit> {
    private final ChatMessageChannelGroupHandler chatMessageServerFacade;
    private final LobbyChannelGroupHandler lobbyServerFacade;

    public ChatMessageServerHandler(ChatMessageChannelGroupHandler chatMessageServerFacade, LobbyChannelGroupHandler lobbyServerFacade) {
        this.chatMessageServerFacade = chatMessageServerFacade;
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext channelHandlerContext, final ChatMessageProtocolDataUnit chatMessagePDU) {
        lobbyServerFacade.findPlayerLobby(channelHandlerContext.channel().id()).ifPresent(lobby ->
                chatMessageServerFacade.receivePlayerLobbyMessage(
                        chatMessagePDU.authorNick(),
                        chatMessagePDU.message(),
                        lobby.stream().filter(channelId -> !channelId.equals(channelHandlerContext.channel().id())).findAny().orElse(null)
                )
        );
    }

    public void receivePlayerLobbyMessage(final String playerNickname, final String playerMessage, final ChannelId receiverId) {
        if (receiverId != null) {
            multicastPDUToClientChannelIds(new ChatMessageProtocolDataUnit(playerNickname, playerMessage), receiverId);
        }
    }

    public void sendPlayerLobbyMessage(final String playerNickname, final String playerMessage) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
