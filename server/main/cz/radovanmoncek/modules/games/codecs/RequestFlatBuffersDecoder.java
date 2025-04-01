package cz.radovanmoncek.modules.games.codecs;

import cz.radovanmoncek.ship.tables.Request;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;

import java.nio.ByteBuffer;

public final class RequestFlatBuffersDecoder extends FlatBuffersDecoder<Request> {

    @Override
    protected boolean decodeHeader(final ByteBuffer in) {

        return in.get() == 'g';
    }

    @Override
    protected Request decodeBodyAfterHeader(final ByteBuffer in) {

        return Request.getRootAsRequest(in);
    }
}
