package cz.radovanmoncek.ship.parents.models;

import com.google.flatbuffers.FlatBufferBuilder;

public interface FlatBufferSerializable {

    byte[] serialize(FlatBufferBuilder builder);
}
