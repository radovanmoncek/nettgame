package container.game.docker.ship.parents.models;

import server.game.docker.modules.chat.pdus.ChatMessageProtocolDataUnit;
import server.game.docker.modules.lobby.pdus.LobbyRequestProtocolDataUnit;
import server.game.docker.modules.lobby.pdus.LobbyResponseProtocolDataUnit;
import server.game.docker.modules.player.pdus.NicknameProtocolDataUnit;

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
 * @see NicknameProtocolDataUnit
 * @see LobbyRequestProtocolDataUnit
 * @see LobbyResponseProtocolDataUnit
 * @see ChatMessageProtocolDataUnit
 */
public interface ProtocolDataUnit {
}
