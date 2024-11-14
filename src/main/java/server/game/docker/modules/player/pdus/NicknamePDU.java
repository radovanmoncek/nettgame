package server.game.docker.modules.player.pdus;

import server.game.docker.ship.parents.pdus.PDU;

public class NicknamePDU implements PDU {
    private String newClientUsername;

    public String getNewNickname() {
        return newClientUsername;
    }

    public void setNewClientUsername(final String newClientUsername) {
        this.newClientUsername = newClientUsername;
    }
}
