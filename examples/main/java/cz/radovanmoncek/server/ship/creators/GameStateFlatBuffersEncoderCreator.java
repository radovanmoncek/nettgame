package cz.radovanmoncek.server.ship.creators;

import cz.radovanmoncek.server.modules.games.codecs.GameStateFlatBuffersEncoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class GameStateFlatBuffersEncoderCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new GameStateFlatBuffersEncoder();
    }
}
