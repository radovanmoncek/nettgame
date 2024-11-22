package server.game.docker.modules.chat.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.modules.chat.facades.ChatMessageServerFacade;
import server.game.docker.modules.chat.pdus.ChatMessagePDU;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;

public final class ChatMessageServerHandler extends SimpleChannelInboundHandler<ChatMessagePDU> {
    private final ChatMessageServerFacade chatMessageServerFacade;
    private final LobbyServerFacade lobbyServerFacade;

    public ChatMessageServerHandler(ChatMessageServerFacade chatMessageServerFacade, LobbyServerFacade lobbyServerFacade) {
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
