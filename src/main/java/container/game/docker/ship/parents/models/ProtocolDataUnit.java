package container.game.docker.ship.parents.models;

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
public interface ProtocolDataUnit {
    int HEADER_SIZE = Byte.BYTES + Long.BYTES;
    byte MIN_PROTOCOL_IDENTIFIER = 0x1;
    byte MAX_PROTOCOL_IDENTIFIER = 0x10;
} //todo: annotation ????
