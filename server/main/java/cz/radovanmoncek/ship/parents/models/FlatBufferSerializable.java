package cz.radovanmoncek.ship.parents.models;

import com.google.flatbuffers.Table;

/**
 * <p>
 * Structure:
 * </p>
 * <pre>
 *     ---------------------------------------------------------
 *     |    Protocol Identifier 0x1    |    Body Length 0x8    | header
 *     ---------------------------------------------------------
 *     |               data (frame max length)                 | body / data / payload
 *     ---------------------------------------------------------
 *     - length is specified by the Body Length header value, please see above.
 * </pre>
 */
public interface FlatBufferSerializable<Schema extends Table> {

    Class<Schema> getSchemaClass();
}
