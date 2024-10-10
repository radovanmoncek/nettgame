package server.game.docker.net.modules.pdus;

import server.game.docker.net.parents.pdus.PDU;

public class ID implements PDU {
    private Long newClientID;

    public Long getNewClientID() {
        return newClientID;
    }

    public void setNewClientID(Long newClientID) {
        this.newClientID = newClientID;
    }
}
