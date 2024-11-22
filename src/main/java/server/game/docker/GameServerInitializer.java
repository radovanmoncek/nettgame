package server.game.docker;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.modules.chat.decoders.ChatMessageDecoder;
import server.game.docker.modules.chat.encoders.ChatMessageEncoder;
import server.game.docker.modules.chat.facades.ChatMessageServerFacade;
import server.game.docker.modules.chat.handlers.ChatMessageServerHandler;
import server.game.docker.modules.lobby.decoder.LobbyRequestDecoder;
import server.game.docker.modules.lobby.encoders.LobbyResponseEncoder;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.modules.lobby.handlers.LobbyServerHandler;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.player.handlers.PlayerServerHandler;
import server.game.docker.modules.session.decoders.SessionDecoder;
import server.game.docker.modules.session.encoders.SessionEncoder;
import server.game.docker.modules.session.facades.SessionServerFacade;
import server.game.docker.modules.session.handlers.SessionServerHandler;
import server.game.docker.modules.state.decoders.StateRequestDecoder;
import server.game.docker.modules.state.encoders.StateResponseEncoder;
import server.game.docker.modules.state.facades.StateServerFacade;

import java.util.function.Supplier;

public final class GameServerInitializer extends ChannelInitializer<SocketChannel>{
    private final PlayerServerFacade playerServerFacade;
    private final LobbyServerFacade lobbyServerFacade;
    private final StateServerFacade stateServerFacade;
    private final Supplier<SessionServerFacade> sessionServerFacadeFactory;
    private final ChatMessageServerFacade chatMessageServerFacade;

    public GameServerInitializer(
            final PlayerServerFacade playerServerFacade,
            final LobbyServerFacade lobbyServerFacade,
            final Supplier<SessionServerFacade> sessionServerFacadeFactory,
            final StateServerFacade stateServerFacade,
            final ChatMessageServerFacade chatMessageServerFacade
    ) {
        this.playerServerFacade = playerServerFacade;
        this.lobbyServerFacade = lobbyServerFacade;
        this.sessionServerFacadeFactory = sessionServerFacadeFactory;
        this.stateServerFacade = stateServerFacade;
        this.chatMessageServerFacade = chatMessageServerFacade;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new StateRequestDecoder(),
                new NicknameDecoder(),
                new LobbyRequestDecoder(),
                new SessionDecoder(),
                new ChatMessageDecoder(),
                new PlayerServerHandler(playerServerFacade),
                new LobbyServerHandler(lobbyServerFacade),
                new ChatMessageServerHandler(chatMessageServerFacade, lobbyServerFacade),
                new SessionServerHandler(sessionServerFacadeFactory, lobbyServerFacade, playerServerFacade, stateServerFacade),
                new NicknameEncoder(),
                new LobbyResponseEncoder(),
                new StateResponseEncoder(),
                new SessionEncoder(),
                new ChatMessageEncoder()
        );
    }
}
