package container.game.docker.ship.bootstrap.examples;

import container.game.docker.modules.examples.chats.codecs.ChatMessageDecoder;
import container.game.docker.modules.examples.chats.codecs.ChatMessageEncoder;
import container.game.docker.modules.examples.chats.handlers.ChatChannelGroupHandler;
import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.codecs.LobbyRequestDecoder;
import container.game.docker.modules.examples.lobbies.codecs.LobbyResponseEncoder;
import container.game.docker.modules.examples.lobbies.handlers.LobbyChannelGroupHandler;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.codes.SessionRequestDecoder;
import container.game.docker.modules.examples.sessions.codes.SessionResponseEncoder;
import container.game.docker.modules.examples.sessions.handlers.SessionChannelGroupHandler;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.bootstrap.InstanceContainer;

public final class SampleInstanceContainer {

    public static void main(String[] args) {

        InstanceContainer.newInstance()
                    .withDecoderFactory(SessionRequestDecoder::new)
                    .withDecoderFactory(LobbyRequestDecoder::new)
                    .withDecoderFactory(ChatMessageDecoder::new)
                    .withChannelGroupHandlerFactory(SessionChannelGroupHandler::new)
                    .withChannelGroupHandlerFactory(LobbyChannelGroupHandler::new)
                    .withChannelGroupHandlerFactory(ChatChannelGroupHandler::new)
                    .withEncoderFactory(SessionResponseEncoder::new)
                    .withEncoderFactory(LobbyResponseEncoder::new)
                    .withEncoderFactory(ChatMessageEncoder::new)
                    .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 1, ChatMessageProtocolDataUnit.class)
                    .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 2, LobbyRequestProtocolDataUnit.class)
                    .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 3, LobbyResponseProtocolDataUnit.class)
                    .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                    .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
                    .run();
    }
}
