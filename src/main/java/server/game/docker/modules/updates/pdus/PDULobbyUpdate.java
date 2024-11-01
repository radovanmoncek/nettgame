package server.game.docker.modules.updates.pdus;

import server.game.docker.ship.enums.PDUType;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 *     Reliably transmitted variable length {@link PDU} with a length of 10B + 4 * 8B (Long)
 * </p>
 * PDU
 * <pre>
 *     --------------------------------
 *     | StateFlag(1B) |  LobbyID(8B) |
 *     --------------------------------
 *     |       Leader bool (1B)       |
 *     --------------------------------
 *     |        Members(max 4*8B)     |
 *     --------------------------------
 * </pre>
 */
public class PDULobbyUpdate implements PDU {
    public static final Byte CREATED = 0;
    public static final Byte JOINED = 1;
    public static final Byte LEFT = 2;
    public static final Byte MEMBERJOINED = 3;
    public static final Byte MEMBERLEFT = 4;
    public static final PDUType type = PDUType.LOBBYUPDATE;
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
     * The id of the lobby.
     */
    private Long lobbyId;
    /**
     * Whether, in the current user context, this user is a leader of this lobby.
     */
    private Boolean leader;
    /**
     * An immutable {@link Collection} of the members currently in this lobby.
     */
    private Collection<Long> members;

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Boolean isLeader() {
        return leader;
    }

    public void setLeader(Boolean leader) {
        this.leader = leader;
    }

    public Collection<Long> getMembers() {
        return members;
    }

    public void setMembers(Collection<Long> members) {
        this.members = Collections.unmodifiableCollection(members);
    }

    public int getStateFlag() {
        return stateFlag;
    }

    public void setStateFlag(Byte stateFlag) {
        this.stateFlag = stateFlag;
    }
}
