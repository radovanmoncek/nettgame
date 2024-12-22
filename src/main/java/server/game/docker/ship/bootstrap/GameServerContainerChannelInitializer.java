package server.game.docker.ship.bootstrap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.modules.chat.decoders.ChatMessageDecoder;
import server.game.docker.modules.chat.encoders.ChatMessageEncoder;
import server.game.docker.modules.chat.facades.ChatMessageChannelGroupFacade;
import server.game.docker.modules.chat.handlers.ChatMessageServerHandler;
import server.game.docker.modules.lobby.decoder.LobbyRequestDecoder;
import server.game.docker.modules.lobby.encoders.LobbyResponseEncoder;
import server.game.docker.modules.lobby.facades.LobbyChannelGroupFacade;
import server.game.docker.modules.lobby.handlers.LobbyServerHandler;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.player.facades.PlayerChannelGroupFacade;
import server.game.docker.modules.player.handlers.PlayerServerHandler;
import server.game.docker.modules.session.decoders.SessionDecoder;
import server.game.docker.modules.session.encoders.SessionEncoder;
import server.game.docker.modules.session.facades.SessionChannelGroupFacade;
import server.game.docker.modules.session.handlers.SessionSimpleChannelInboundHandler;
import server.game.docker.modules.state.decoders.StateRequestDecoder;
import server.game.docker.modules.state.encoders.StateResponseEncoder;
import server.game.docker.modules.state.facades.StateChannelGroupFacade;

import java.util.function.Supplier;

public final class GameServerContainerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final PlayerChannelGroupFacade playerChannelGroupFacade;
    private final LobbyChannelGroupFacade lobbyChannelGroupFacade;
    private final ChatMessageChannelGroupFacade chatMessageChannelGroupFacade;
    private final Supplier<SessionChannelGroupFacade> sessionChannelGroupFacadeSupplier;
    private final StateChannelGroupFacade stateChannelGroupFacade;
    private final ThreadGroup managedSessions;

    public GameServerContainerChannelInitializer(
            final PlayerChannelGroupFacade playerServerFacade,
            final LobbyChannelGroupFacade lobbyChannelGroupFacade,
            final ChatMessageChannelGroupFacade chatMessageChannelGroupFacade,
            final Supplier<SessionChannelGroupFacade> sessionChannelGroupFacadeSupplier,
            final StateChannelGroupFacade stateChannelGroupFacade,
            final ThreadGroup managedSessions
    ) {
        this.playerChannelGroupFacade = playerServerFacade;
        this.lobbyChannelGroupFacade = lobbyChannelGroupFacade;
        this.chatMessageChannelGroupFacade = chatMessageChannelGroupFacade;
        this.sessionChannelGroupFacadeSupplier = sessionChannelGroupFacadeSupplier;
        this.stateChannelGroupFacade = stateChannelGroupFacade;
        this.managedSessions = managedSessions;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new StateRequestDecoder(),
                new SessionDecoder(),
                new LobbyRequestDecoder(),
                new NicknameDecoder(),
                new ChatMessageDecoder(),
                new PlayerServerHandler(playerChannelGroupFacade),
                new LobbyServerHandler(lobbyChannelGroupFacade),
                new ChatMessageServerHandler(chatMessageChannelGroupFacade, lobbyChannelGroupFacade),
                new SessionSimpleChannelInboundHandler(sessionChannelGroupFacadeSupplier, stateChannelGroupFacade, lobbyChannelGroupFacade, managedSessions),
                new NicknameEncoder(),
                new LobbyResponseEncoder(),
                new ChatMessageEncoder(),
                new StateResponseEncoder(),
                new SessionEncoder()
        );
    }
}
