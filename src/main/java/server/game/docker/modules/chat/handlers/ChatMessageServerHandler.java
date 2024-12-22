package server.game.docker.modules.chat.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.chat.facades.ChatMessageChannelGroupFacade;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;
import server.game.docker.modules.lobby.facades.LobbyChannelGroupFacade;

public final class ChatMessageServerHandler extends SimpleChannelInboundHandler<ChatMessagePDU> {
    private final ChatMessageChannelGroupFacade chatMessageServerFacade;
    private final LobbyChannelGroupFacade lobbyServerFacade;

    public ChatMessageServerHandler(ChatMessageChannelGroupFacade chatMessageServerFacade, LobbyChannelGroupFacade lobbyServerFacade) {
        this.chatMessageServerFacade = chatMessageServerFacade;
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext channelHandlerContext, final ChatMessagePDU chatMessagePDU) {
        lobbyServerFacade.findPlayerLobby(channelHandlerContext.channel().id()).ifPresent(lobby ->
                chatMessageServerFacade.receivePlayerLobbyMessage(
                        chatMessagePDU.authorNick(),
                        chatMessagePDU.message(),
                        lobby.stream().filter(channelId -> !channelId.equals(channelHandlerContext.channel().id())).findAny().orElse(null)
                )
        );
    }
}
