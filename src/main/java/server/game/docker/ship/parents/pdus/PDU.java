package server.game.docker.ship.parents.pdus;

/**
 * <p>
 *     This tagging interface is a high level representation of a PDU (Protocol Data Unit) for easier processing.
 * </p>
 * PDU:
 * <pre>
 *     ---------------------------------------------------
 *     | PDUType byte (1B)     |    dataLength int? (4B) |
 *     ---------------------------------------------------
 *     |                  data (frame max length)        |
 *     ---------------------------------------------------
 * </pre>
 */
public interface PDU {
}
