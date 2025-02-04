package container.game.docker.ship.bootstrap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import container.game.docker.modules.chat.examples.decoders.ChatMessageDecoder;
import container.game.docker.modules.chat.examples.encoders.ChatMessageEncoder;
import server.game.docker.modules.chat.facades.ChatMessageChannelGroupHandler;
import container.game.docker.modules.chat.examples.handlers.ChatMessageServerHandler;
import server.game.docker.modules.lobby.decoder.LobbyRequestDecoder;
import server.game.docker.modules.lobby.encoders.LobbyResponseEncoder;
import server.game.docker.modules.lobby.facades.LobbyChannelGroupHandler;
import server.game.docker.modules.lobby.handlers.LobbySimpleChannelInboundHandlerHandler;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.player.facades.PlayerChannelGroupHandler;
import server.game.docker.modules.player.handlers.PlayerServerHandler;
import server.game.docker.modules.session.decoders.SessionDecoder;
import server.game.docker.modules.session.encoders.SessionEncoder;
import server.game.docker.modules.session.facades.SessionChannelGroupHandler;
import server.game.docker.modules.session.handlers.SessionSimpleChannelInboundHandler;
import server.game.docker.modules.state.decoders.StateRequestDecoder;
import server.game.docker.modules.state.encoders.StateResponseEncoder;
import server.game.docker.modules.state.facades.StateChannelGroupHandler;

import java.util.function.Supplier;

public final class InstanceContainerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final PlayerChannelGroupHandler playerChannelGroupFacade;
    private final LobbyChannelGroupHandler lobbyChannelGroupFacade;
    private final ChatMessageChannelGroupHandler chatMessageChannelGroupFacade;
    private final Supplier<SessionChannelGroupHandler> sessionChannelGroupFacadeSupplier;
    private final StateChannelGroupHandler stateChannelGroupFacade;
    private final ThreadGroup managedSessions;

    public InstanceContainerChannelInitializer(
            final PlayerChannelGroupHandler playerServerFacade,
            final LobbyChannelGroupHandler lobbyChannelGroupFacade,
            final ChatMessageChannelGroupHandler chatMessageChannelGroupFacade,
            final Supplier<SessionChannelGroupHandler> sessionChannelGroupFacadeSupplier,
            final StateChannelGroupHandler stateChannelGroupFacade,
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
                new LobbySimpleChannelInboundHandlerHandler(lobbyChannelGroupFacade),
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
