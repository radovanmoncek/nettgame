package server.game.docker.client.ship.bootstrap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.client.modules.lobby.decoders.LobbyResponseDecoder;
import server.game.docker.client.modules.lobby.encoders.LobbyRequestEncoder;
import server.game.docker.client.modules.lobby.facades.LobbyChannelFacade;
import server.game.docker.client.modules.lobby.handlers.LobbyClientHandler;
import server.game.docker.client.modules.messages.facades.ChatMessageChannelFacade;
import server.game.docker.client.modules.messages.handlers.ChatMessageClientHandler;
import server.game.docker.client.modules.player.facades.PlayerChannelFacade;
import server.game.docker.client.modules.player.handlers.PlayerClientHandler;
import server.game.docker.client.modules.sessions.facades.SessionChannelFacade;
import server.game.docker.client.modules.sessions.handlers.SessionClientHandler;
import server.game.docker.client.modules.state.decoders.StateResponseDecoder;
import server.game.docker.client.modules.state.encoders.StateRequestEncoder;
import server.game.docker.client.modules.state.facades.StateChannelFacade;
import server.game.docker.client.modules.state.handlers.StateClientHandler;
import server.game.docker.modules.chat.decoders.ChatMessageDecoder;
import server.game.docker.modules.chat.encoders.ChatMessageEncoder;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.session.decoders.SessionDecoder;
import server.game.docker.modules.session.encoders.SessionEncoder;

public final class GameClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final PlayerChannelFacade playerClientFacade;
    private final LobbyChannelFacade lobbyClientFacade;
    private final SessionChannelFacade sessionClientFacade;
    private final StateChannelFacade stateClientFacade;
    private final ChatMessageChannelFacade chatMessageClientFacade;

    public GameClientChannelInitializer(
            final PlayerChannelFacade playerClientFacade,
            final LobbyChannelFacade lobbyClientFacade,
            final SessionChannelFacade sessionClientFacade,
            final StateChannelFacade stateClientFacade,
            final ChatMessageChannelFacade chatMessageClientFacade
    ) {
        this.playerClientFacade = playerClientFacade;
        this.lobbyClientFacade = lobbyClientFacade;
        this.sessionClientFacade = sessionClientFacade;
        this.stateClientFacade = stateClientFacade;
        this.chatMessageClientFacade = chatMessageClientFacade;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new StateResponseDecoder(),
                new NicknameDecoder(),
                new LobbyResponseDecoder(),
                new SessionDecoder(),
                new ChatMessageDecoder(),
                new PlayerClientHandler(playerClientFacade),
                new LobbyClientHandler(lobbyClientFacade),
                new StateClientHandler(stateClientFacade),
                new ChatMessageClientHandler(chatMessageClientFacade),
                new SessionClientHandler(sessionClientFacade),
                new NicknameEncoder(),
                new LobbyRequestEncoder(),
                new StateRequestEncoder(),
                new SessionEncoder(),
                new ChatMessageEncoder()
        );
    }
}
