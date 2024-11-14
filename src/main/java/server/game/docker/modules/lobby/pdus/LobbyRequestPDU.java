package server.game.docker.modules.lobby.pdus;

import server.game.docker.ship.parents.pdus.PDU;

/**
 * Reliably transported PDU with 1B (Byte) + 4B (Long) payload.
 */
public class LobbyRequestPDU implements PDU {
    public static final Byte CREATE = 0;
    public static final Byte JOIN = 1;
    public static final Byte LEAVE = 2;
    public static final Byte INFO = 3;
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
     *     <li>
     *         3 = info
     *     </li>
     * </ul>
     */
    private Byte actionFlag;
    /**
     * Not required, unless {@link #actionFlag} is set to 1 (join).
     */
    private Long leaderId;

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(final Long leaderId) {
        this.leaderId = leaderId;
    }

    public Byte getActionFlag() {
        return actionFlag;
    }

    public void setActionFlag(Byte actionFlag) {
        this.actionFlag = actionFlag;
    }
}
