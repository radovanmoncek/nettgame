package server.game.docker.modules.lobby.pdus;

import server.game.docker.ship.parents.pdus.PDU;

import java.util.Collection;

/**
 * <p>
 *     Reliably transmitted variable length {@link PDU} with a length of 10B + 4 * 8B (Long)
 * </p>
 * PDU
 * <pre>
 *     --------------------------------
 *     | StateFlag(1B) | LeaderId(8B) |
 *     --------------------------------
 *     |        Members(max 4*8B)     |
 *     --------------------------------
 * </pre>
 */
public class LobbyUpdatePDU implements PDU {
    public static final Byte CREATED = 0;
    public static final Byte JOINED = 1;
    public static final Byte LEFT = 2;
    public static final Byte MEMBERJOINED = 3;
    public static final Byte MEMBERLEFT = 4;
    /**
     * <ul>
     *     <li>
     *         0 = created
     *     </li>
     *     <li>
     *         1 = joined
     *     </li>
     *     <li>
     *         2 = left (no further information follows)
     *     </li>
     *     <li>
     *         3 = member joined
     *     </li>
     *     <li>
     *         4 = member left
     *     </li>
     * </ul>
     */
    private Byte stateFlag;
    /**
     * Whether, in the current user context, this user is a leader of this lobby.
     */
    private Long leaderId;
    private Collection<String> members;

    public Byte getStateFlag() {
        return stateFlag;
    }

    public void setStateFlag(Byte stateFlag) {
        this.stateFlag = stateFlag;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public Collection<String> getMembers() {
        return members;
    }

    public void setMembers(Collection<String> members) {
        this.members = members;
    }
}
