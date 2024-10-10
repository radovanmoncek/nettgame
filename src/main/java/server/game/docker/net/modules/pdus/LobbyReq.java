package server.game.docker.net.modules.pdus;

import server.game.docker.net.parents.pdus.PDU;

/**
 * Reliably transported PDU with 1B (Byte) + 4B (Long) payload.
 */
public class LobbyReq implements PDU {
    /**
     * <ul>
     *     <li>
     *         0 = create
     *     </li>
     *     <li>
     *         1 = join (requires lobby id also)
     *     </li>
     *     <li>
     *         2 = leave
     *     </li>
     * </ul>
     */
    private Byte actionFlag;
    /**
     * Not required, unless {@link #actionFlag} is set to 1 (join).
     */
    private Long lobbyID;

    public Long getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(Long lobbyID) {
        this.lobbyID = lobbyID;
    }

    public Byte getActionFlag() {
        return actionFlag;
    }

    public void setActionFlag(Byte actionFlag) {
        this.actionFlag = actionFlag;
    }
}
