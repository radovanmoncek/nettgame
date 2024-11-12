package server.game.docker.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.client.modules.lobby.encoders.LobbyRequestEncoder;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.lobby.handlers.LobbyClientHandler;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;
import server.game.docker.client.modules.player.handlers.PlayerClientHandler;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;

public final class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final PlayerClientFacade playerClientFacade;
    private final LobbyClientFacade lobbyClientFacade;

    public ClientInitializer(final PlayerClientFacade playerClientFacade, final LobbyClientFacade lobbyClientFacade) {
        this.playerClientFacade = playerClientFacade;
        this.lobbyClientFacade = lobbyClientFacade;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new NicknameDecoder(),
                new PlayerClientHandler(playerClientFacade),
                new LobbyClientHandler(lobbyClientFacade),
                new NicknameEncoder(),
                new LobbyRequestEncoder()
        );
    }
}
