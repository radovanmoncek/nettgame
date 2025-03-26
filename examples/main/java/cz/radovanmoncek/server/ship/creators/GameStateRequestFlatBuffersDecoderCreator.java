package cz.radovanmoncek.server.ship.creators;

import cz.radovanmoncek.server.modules.games.codecs.GameStateRequestFlatBuffersDecoder;
import cz.radovanmoncek.ship.parents.creators.ChannelHandlerCreator;
import io.netty.channel.ChannelHandler;

public class GameStateRequestFlatBuffersDecoderCreator extends ChannelHandlerCreator {

    @Override
    public ChannelHandler newProduct() {

        return new GameStateRequestFlatBuffersDecoder();
    }
}
