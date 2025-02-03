package client.ship.bootstrap;

import client.modules.lobby.encoders.LobbyRequestEncoder;
import client.modules.lobby.handlers.LobbyClientHandler;
import client.modules.messages.handlers.ChatMessageClientHandler;
import client.modules.player.handlers.PlayerClientHandler;
import client.modules.sessions.handlers.SessionClientHandler;
import client.modules.state.decoders.StateResponseDecoder;
import client.modules.state.encoders.StateRequestEncoder;
import client.modules.state.handlers.StateClientHandler;
import client.ship.parents.facades.ChannelPDUCommunicationsHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import client.modules.lobby.decoders.LobbyResponseDecoder;
import server.game.docker.modules.chat.decoders.ChatMessageDecoder;
import server.game.docker.modules.chat.encoders.ChatMessageEncoder;
import server.game.docker.modules.player.decoders.NicknameDecoder;
import server.game.docker.modules.player.encoders.NicknameEncoder;
import server.game.docker.modules.session.decoders.SessionDecoder;
import server.game.docker.modules.session.encoders.SessionEncoder;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.function.Supplier;

public final class GameClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> playerClientHandlerSupplier;
    private final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> lobbyClientHandlerSupplier;
    private final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> sessionClientHandlerSupplier;
    private final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> stateClientHandlerSupplier;
    private final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> chatMessageClientHandlerSupplier;

    public GameClientChannelInitializer(
            final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> playerClientHandlerSupplier,
            final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> lobbyClientHandlerSupplier,
            final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> sessionClientHandlerSupplier,
            final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> stateClientHandlerSupplier,
            final Supplier<ChannelPDUCommunicationsHandler<? extends PDU>> chatMessageClientHandlerSupplier
    ) {
        this.playerClientHandlerSupplier = playerClientHandlerSupplier;
        this.lobbyClientHandlerSupplier = lobbyClientHandlerSupplier;
        this.sessionClientHandlerSupplier = sessionClientHandlerSupplier;
        this.stateClientHandlerSupplier = stateClientHandlerSupplier;
        this.chatMessageClientHandlerSupplier = chatMessageClientHandlerSupplier;
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
                playerClientHandlerSupplier.get(),
                lobbyClientHandlerSupplier.get(),
                stateClientHandlerSupplier.get(),
                chatMessageClientHandlerSupplier.get(),
                sessionClientHandlerSupplier.get(),
                new NicknameEncoder(),
                new LobbyRequestEncoder(),
                new StateRequestEncoder(),
                new SessionEncoder(),
                new ChatMessageEncoder()
        );
    }
}
