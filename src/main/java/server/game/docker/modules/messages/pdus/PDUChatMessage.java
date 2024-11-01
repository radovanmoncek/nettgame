package server.game.docker.modules.messages.pdus;

import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

/**
 * <p>
 *     Reliably transported non-empty variable length PDU (char array max size 64)
 * </p>
 * PDU:
 * <pre>
 *     --------------------------------
 *     | AuthorID(8B) |   Msg(64B)    |
 *     --------------------------------
 * </pre>
 */
public class PDUChatMessage implements PDU {
    public static final PDUType type = PDUType.CHATMESSAGE;
    private Long authorID;
    private String authorName;
    private String message;

    public Long getAuthorID() {
        return authorID;
    }

    public void setAuthorID(Long authorID) {
        this.authorID = authorID;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
