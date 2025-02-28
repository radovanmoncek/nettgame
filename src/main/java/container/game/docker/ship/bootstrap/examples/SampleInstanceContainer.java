package container.game.docker.ship.bootstrap.examples;

import container.game.docker.ship.builders.InstanceContainerBootstrapBuilder;
import container.game.docker.ship.examples.compiled.schemas.ChatMessage;
import container.game.docker.ship.examples.compiled.schemas.GameState;
import container.game.docker.ship.examples.compiled.schemas.GameStateRequest;
import container.game.docker.ship.examples.creators.*;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public final class SampleInstanceContainer {

    public static void main(String[] args) {

        new InstanceContainerBootstrapBuilder()
                .buildPort(4321)
                .buildInternetProtocolAddress(InetAddress.getLoopbackAddress())
                .buildChannelHandlerCreator(new SessionRequestDecoderFactory())
                .buildChannelHandlerCreator(new SessionChannelGroupHandlerCreator())
                .buildChannelHandlerCreator(new SessionResponseEncoderCreator())
                .buildLogLevel(LogLevel.INFO)
                .buildProtocolSchema((byte) 'G', GameState.class)
                .buildProtocolSchema((byte) 'g', GameStateRequest.class)
                .buildProtocolSchema((byte) 'm', ChatMessage.class)
                .build()
                .run();
    }
}
