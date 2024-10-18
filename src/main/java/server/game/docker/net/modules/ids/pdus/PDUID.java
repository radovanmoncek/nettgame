package server.game.docker.net.modules.ids.pdus;

import server.game.docker.net.enums.PDUType;
import server.game.docker.net.parents.pdus.PDU;

public class PDUID implements PDU {
    public static final PDUType type = PDUType.ID;
    private Long newClientID;

    public Long getNewClientID() {
        return newClientID;
    }

    public void setNewClientID(Long newClientID) {
        this.newClientID = newClientID;
    }
}
