package container.game.docker.ship.bootstrap.examples;

import container.game.docker.modules.examples.chat.decoders.ChatMessageDecoder;
import container.game.docker.modules.examples.chat.encoders.ChatMessageEncoder;
import container.game.docker.modules.examples.chat.handlers.ChatChannelGroupHandler;
import container.game.docker.modules.examples.lobby.decoder.LobbyRequestDecoder;
import container.game.docker.modules.examples.lobby.encoders.LobbyResponseEncoder;
import container.game.docker.modules.examples.lobby.handlers.LobbyChannelGroupHandler;
import container.game.docker.modules.examples.session.decoders.SessionRequestDecoder;
import container.game.docker.modules.examples.session.encoders.SessionResponseEncoder;
import container.game.docker.modules.examples.session.handlers.SessionChannelGroupHandler;
import container.game.docker.ship.bootstrap.InstanceContainer;

public final class SampleInstanceContainer {

    public static void main(String[] args) throws Exception {
            InstanceContainer.newInstance()
                    .withDecoderSupplier(SessionRequestDecoder::new)
                    .withDecoderSupplier(LobbyRequestDecoder::new)
                    .withDecoderSupplier(ChatMessageDecoder::new)
                    .withChannelGroupHandlerSupplier(SessionChannelGroupHandler::new)
                    .withChannelGroupHandlerSupplier(LobbyChannelGroupHandler::new)
                    .withChannelGroupHandlerSupplier(ChatChannelGroupHandler::new)
                    .withEncoderSupplier(SessionResponseEncoder::new)
                    .withEncoderSupplier(LobbyResponseEncoder::new)
                    .withEncoderSupplier(ChatMessageEncoder::new)
                    .run();
    }
}
