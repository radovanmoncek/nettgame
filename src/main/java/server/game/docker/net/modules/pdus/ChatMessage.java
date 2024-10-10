package server.game.docker.net.modules.pdus;

import server.game.docker.net.parents.pdus.PDU;

/**
 * Reliably transported non-empty variable length PDU (char array max size 64)
 */
public class ChatMessage implements PDU {
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
