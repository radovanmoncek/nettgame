package container.game.docker.modules.examples.chats.codecs;

import com.google.flatbuffers.FlatBufferBuilder;
import container.game.docker.modules.examples.chats.models.ChatMessageFlatBufferSerializable;
import container.game.docker.ship.parents.codecs.FlatBuffersEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class ChatMessageFlatBuffersEncoder extends FlatBuffersEncoder<ChatMessageFlatBufferSerializable> {

    @Override
    protected byte[] encodeBodyAfterHeader(ChatMessageFlatBufferSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {
        return new byte[0];
    }
}
