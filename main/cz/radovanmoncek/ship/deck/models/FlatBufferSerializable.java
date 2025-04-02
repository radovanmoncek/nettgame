package cz.radovanmoncek.ship.deck.models;

import com.google.flatbuffers.FlatBufferBuilder;

public interface FlatBufferSerializable {

    byte[] serialize(FlatBufferBuilder builder);
}
