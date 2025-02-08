package client.game.docker.modules.examples.lobbies.handlers;

import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.lobby.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobby.models.LobbyResponseProtocolDataUnit;

import javax.swing.*;

public class LobbyChannelHandler extends ChannelHandler<LobbyResponseProtocolDataUnit, LobbyRequestProtocolDataUnit> {
    private final JPanel client;

    public LobbyChannelHandler(final SampleGameClient sampleGameClient) {

        client = sampleGameClient;
    }

    @Override
    protected void serverChannelRead(final LobbyResponseProtocolDataUnit lobbyUpdate) {}
}
