package client.game.docker.ship.bootstrap.examples;

import client.game.docker.modules.examples.chats.handlers.ChatChannelHandler;
import client.game.docker.modules.examples.lobbies.codecs.LobbyRequestEncoder;
import client.game.docker.modules.examples.lobbies.codecs.LobbyResponseDecoder;
import client.game.docker.modules.examples.lobbies.handlers.LobbyChannelHandler;
import client.game.docker.modules.examples.sessions.codecs.SessionRequestEncoder;
import client.game.docker.modules.examples.sessions.codecs.SessionResponseDecoder;
import client.game.docker.modules.examples.sessions.handlers.SessionChannelHandler;
import client.game.docker.ship.bootstrap.GameClient;
import container.game.docker.modules.examples.chats.codecs.ChatMessageDecoder;
import container.game.docker.modules.examples.chats.codecs.ChatMessageEncoder;
import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;

public final class SampleGameClient {

    public static void main(String[] args) {

        GameClient
                .newInstance()
                .withDecoder(new SessionResponseDecoder())
                .withDecoder(new LobbyResponseDecoder())
                .withDecoder(new ChatMessageDecoder())
                .withChannelHandler(new LobbyChannelHandler())
                .withChannelHandler(new SessionChannelHandler())
                .withChannelHandler(new ChatChannelHandler())
                .withEncoder(new SessionRequestEncoder())
                .withEncoder(new LobbyRequestEncoder())
                .withEncoder(new ChatMessageEncoder())
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 1, ChatMessageProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 2, LobbyRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 3, LobbyResponseProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                .registerProtocolDataUnitToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
                .run();
    }
}
