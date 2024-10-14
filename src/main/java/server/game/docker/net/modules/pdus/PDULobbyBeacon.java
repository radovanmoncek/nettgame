package server.game.docker.net.modules.pdus;

import server.game.docker.net.parents.pdus.PDU;

/**
 * <p>
 *     Reliably transported PDU with a fixed length of 8B + 1B + 1B + 1B ({@link Boolean}).
 * </p>
 * PDU
 * <pre>
 *     --------------------------------
 *     |            LobbyID(8B)       |
 *     --------------------------------
 *     |           |         |        |
 *     --------------------------------
 * </pre>
 */
public class PDULobbyBeacon implements PDU {
    private Long lobbyID;
    private Byte lobbyCurOccupancy;
    private Byte lobbyMaxOccupancy;
    private Boolean lobbyListRefresh;

    public long getLobbyID() {
        return lobbyID;
    }

    public Byte getLobbyCurOccupancy() {
        return lobbyCurOccupancy;
    }

    public Byte getLobbyMaxOccupancy() {
        return lobbyMaxOccupancy;
    }

    public void setLobbyID(Long lobbyID) {
        this.lobbyID = lobbyID;
    }

    public void setLobbyCurOccupancy(Byte lobbyCurOccupancy) {
        this.lobbyCurOccupancy = lobbyCurOccupancy;
    }

    public void setLobbyMaxOccupancy(Byte lobbyMaxOccupancy) {
        this.lobbyMaxOccupancy = lobbyMaxOccupancy;
    }

    public Boolean getLobbyListRefresh() {
        return lobbyListRefresh;
    }

    public void setLobbyListRefresh(Boolean lobbyListRefresh) {
        this.lobbyListRefresh = lobbyListRefresh;
    }

    @Override
    public String toString() {
        return "PDULobbyBeacon{" +
                "lobbyID=" + lobbyID +
                ", lobbyCurOccupancy=" + lobbyCurOccupancy +
                ", lobbyMaxOccupancy=" + lobbyMaxOccupancy +
                ", lobbyListRefresh=" + lobbyListRefresh +
                '}';
    }
}
