package container.game.docker.ship.bootstrap.examples;

import container.game.docker.modules.examples.chats.models.ChatMessageProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.bootstrap.InstanceContainer;
import container.game.docker.ship.examples.creators.*;

public final class SampleInstanceContainer {

    public static void main(String[] args) {

        InstanceContainer
                .newInstance()
                .withPlayerSessionDataCreator(new PlayerSessionDataCreator())
                .withDecoderCreator(new SessionRequestDecoderFactory())
                .withDecoderCreator(new LobbyRequestDecoderCreator())
                .withDecoderCreator(new ChatMessageDecoderCreator())
                .withChannelGroupHandlerCreator(new SessionChannelGroupHandlerCreator())
                .withChannelGroupHandlerCreator(new LobbyChannelGroupHandlerCreator())
                .withChannelGroupHandlerCreator(new ChatChannelGroupHandlerCreator())
                .withEncoderCreator(new SessionResponseEncoderCreator())
                .withEncoderCreator(new LobbyResponseEncoderCreator())
                .withEncoderCreator(new ChatMessageEncoderCreator())
                .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 1, ChatMessageProtocolDataUnit.class)
                .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 2, LobbyRequestProtocolDataUnit.class)
                .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 3, LobbyResponseProtocolDataUnit.class)
                .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 4, SessionRequestProtocolDataUnit.class)
                .registerProtocolDataUnitIdentifierToProtocolDataUnitBinding((byte) 5, SessionResponseProtocolDataUnit.class)
                .run();
    }
}
