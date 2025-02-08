package container.game.docker.ship.parents.models;

/**
 * <p>
 * Structure:
 * </p>
 * <pre>
 *     ---------------------------------------------------
 *     | Type byte (1B)     |    bodyLength int? (4B)    |
 *     ---------------------------------------------------
 *     |                  data (frame max length)        |
 *     ---------------------------------------------------
 * </pre>
 */
public interface ProtocolDataUnit {

    int getProtocolIdentifier();

    long getBodyLength();
}
