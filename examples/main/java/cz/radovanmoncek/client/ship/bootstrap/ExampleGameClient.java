package cz.radovanmoncek.client.ship.bootstrap;

import cz.radovanmoncek.client.modules.games.codecs.GameStateFlatBufferDecoder;
import cz.radovanmoncek.client.modules.games.codecs.GameStateRequestFlatBufferEncoder;
import cz.radovanmoncek.client.modules.games.handlers.ExampleServerChannelHandler;
import cz.radovanmoncek.client.ship.builders.GameClientBootstrapBuilder;
import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public final class ExampleGameClient {

    public static void main(String[] args) {

        new GameClientBootstrapBuilder()
                .buildLogLevel(LogLevel.INFO)
                .buildServerAddress(InetAddress.getLoopbackAddress())
                .buildPort(4321)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'g', GameStateRequest.class)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'G', GameState.class)
                .buildMagicByteToFlatBuffersSchemaBinding((byte) 'm', ChatMessage.class)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(new ExampleServerChannelHandler())
                .buildChannelHandler(new GameStateRequestFlatBufferEncoder())
                .build()
                .run();
    }
}
