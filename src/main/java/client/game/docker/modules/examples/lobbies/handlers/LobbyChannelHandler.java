package client.game.docker.modules.examples.lobbies.handlers;

import client.game.docker.ship.bootstrap.examples.SampleGameClient;
import client.game.docker.ship.parents.handlers.ChannelHandler;
import container.game.docker.modules.examples.lobbies.models.LobbyRequestProtocolDataUnit;
import container.game.docker.modules.examples.lobbies.models.LobbyResponseProtocolDataUnit;

import javax.swing.*;

public class LobbyChannelHandler extends ChannelHandler<LobbyResponseProtocolDataUnit, LobbyRequestProtocolDataUnit> {


    public LobbyChannelHandler() {}

    @Override
    protected void serverChannelRead(final LobbyResponseProtocolDataUnit lobbyUpdate) {}
}
