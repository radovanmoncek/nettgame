package server.game.docker.modules.usernames.pdus;

import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

public class UsernamePDU implements PDU {
    public static final PDUType type = PDUType.USERNAME;
    private String newClientUsername;

    public String getNewClientUsername() {
        return newClientUsername;
    }

    public void setNewClientUsername(final String newClientUsername) {
        this.newClientUsername = newClientUsername;
    }
}
