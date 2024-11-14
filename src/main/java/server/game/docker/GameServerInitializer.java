package server.game.docker;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.modules.lobby.decoder.LobbyRequestDecoder;
import server.game.docker.modules.lobby.encoders.LobbyUpdateEncoder;
import server.game.docker.modules.lobby.facades.LobbyServerFacade;
import server.game.docker.modules.lobby.handlers.LobbyServerHandler;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.player.facades.PlayerServerFacade;
import server.game.docker.modules.player.handlers.PlayerServerHandler;

public final class GameServerInitializer extends ChannelInitializer<SocketChannel>{
    private final PlayerServerFacade playerServerFacade;
    private final LobbyServerFacade lobbyServerFacade;

    public GameServerInitializer(final PlayerServerFacade playerServerFacade, final LobbyServerFacade lobbyServerFacade) {
        this.playerServerFacade = playerServerFacade;
        this.lobbyServerFacade = lobbyServerFacade;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new NicknameDecoder(),
                new LobbyRequestDecoder(),
                new PlayerServerHandler(playerServerFacade),
                new LobbyServerHandler(lobbyServerFacade),
                new NicknameEncoder(),
                new LobbyUpdateEncoder()
        );
    }
}
