package server.game.docker.client;

import io.netty.channel.Channel;
import server.game.docker.GameServerInitializer;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Map;

public final class ClientInitializer {
    private final GameClient gameClient;
    private final GameServerInitializer.RouterHandler multiPipeline;

    public ClientInitializer(Channel clientChannel, Map<GameClient.ClientAPIEventType, GameClient.ClientAPIEventHandler<? extends PDU>> eventMappings, GameClient gameClient, GameServerInitializer.RouterHandler multiPipeline) {
        this.gameClient = gameClient;
        this.multiPipeline = multiPipeline;
    }

    public void init() {
    }
}
