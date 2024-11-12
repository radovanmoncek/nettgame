package server.game.docker.ship.parents.pdus;

import server.game.docker.modules.player.pdus.NicknamePDU;

/**
 * <p>
 * This tagging interface serves as a marker of high level representations of a PDU (Protocol Data Unit) for easier processing.
 * </p>
 * <p>
 * base PDU structure:
 * </p>
 * <pre>
 *     ---------------------------------------------------
 *     | PDUType byte (1B)     |    dataLength int? (4B) |
 *     ---------------------------------------------------
 *     |                  data (frame max length)        |
 *     ---------------------------------------------------
 * </pre>
 * @see NicknamePDU
 * @see server.game.docker.modules.lobby.pdus.LobbyRequestPDU
 * @see server.game.docker.modules.lobby.pdus.LobbyUpdatePDU
 * @see server.game.docker.modules.chat.pdus.ChatMessagePDU
 */
public interface PDU {
}
