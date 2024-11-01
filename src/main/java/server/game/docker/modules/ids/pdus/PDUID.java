package server.game.docker.modules.ids.pdus;

import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

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
