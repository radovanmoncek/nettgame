package cz.radovanmoncek.nettgame.nettgame.ship.deck.models;

import com.google.flatbuffers.FlatBufferBuilder;

/**
 * This class serves as a "band-aid" to transfer data to {@link cz.radovanmoncek.nettgame.nettgame.ship.bay.parents.codecs.FlatBuffersEncoder}.
 * @since 1.0
 * @author Radovan Monček
 */
public interface FlatBufferSerializable {

    /**
     * Serialise itself into binary data format using FlatBuffers.
     * @param builder convenience FlatBuffersBuilder instance supplied by the codec.
     * @return the message in binary format.
     * @since 1.0
     * @author Radovan Monček
     */
    byte[] serialize(FlatBufferBuilder builder);
}
