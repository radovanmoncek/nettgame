package server.game.docker;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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
    private final Supplier<SessionServerFacade> sessionServerFacadePrototype;

    public GameServerInitializer(final PlayerServerFacade playerServerFacade, final LobbyServerFacade lobbyServerFacade, Supplier<SessionServerFacade> sessionServerFacadePrototype, StateServerFacade stateServerFacade) {
        this.playerServerFacade = playerServerFacade;
        this.lobbyServerFacade = lobbyServerFacade;
        this.sessionServerFacadePrototype = sessionServerFacadePrototype;
        this.stateServerFacade = stateServerFacade;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new NicknameDecoder(),
                new LobbyRequestDecoder(),
                new SessionDecoder(),
                new StateRequestDecoder(),
                new PlayerServerHandler(playerServerFacade),
                new LobbyServerHandler(lobbyServerFacade),
                new SessionServerHandler(sessionServerFacadePrototype, lobbyServerFacade, playerServerFacade, stateServerFacade),
                new NicknameEncoder(),
                new LobbyResponseEncoder(),
                new StateResponseEncoder(),
                new SessionEncoder()
        );
    }
}
