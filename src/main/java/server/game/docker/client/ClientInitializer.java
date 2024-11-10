package server.game.docker.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.game.docker.client.modules.requests.encoders.LobbyReqEncoder;
import server.game.docker.client.modules.requests.facades.LobbyRequestClientFacade;
import server.game.docker.client.modules.usernames.facades.UsernameClientFacade;
import server.game.docker.client.modules.usernames.handlers.UsernameClientHandler;
import server.game.docker.modules.usernames.decoders.UsernameDecoder;
import server.game.docker.modules.usernames.encoders.UsernameEncoder;

public final class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final UsernameClientFacade usernameClientFacade;
    private final LobbyRequestClientFacade lobbyRequestServerFacade;

    public ClientInitializer(final UsernameClientFacade usernameClientFacade, final LobbyRequestClientFacade lobbyRequestServerFacade) {
        this.usernameClientFacade = usernameClientFacade;
        this.lobbyRequestServerFacade = lobbyRequestServerFacade;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addFirst(
                new LoggingHandler(LogLevel.ERROR),
                new UsernameDecoder(),
                new UsernameClientHandler(usernameClientFacade),
                new UsernameEncoder(),
                new LobbyReqEncoder()
        );
    }
}
