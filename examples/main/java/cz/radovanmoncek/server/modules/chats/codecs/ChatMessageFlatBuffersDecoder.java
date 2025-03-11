package cz.radovanmoncek.server.modules.chats.codecs;

import cz.radovanmoncek.server.ship.compiled.schemas.ChatMessage;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersDecoder;

import java.nio.ByteBuffer;

public final class ChatMessageFlatBuffersDecoder extends FlatBuffersDecoder<ChatMessage> {
    @Override
    protected boolean decodeHeader(ByteBuffer in) {
        return false;
    }

    @Override
    protected ChatMessage decodeBodyAfterHeader(ByteBuffer in) {
        return null;
    }
}
