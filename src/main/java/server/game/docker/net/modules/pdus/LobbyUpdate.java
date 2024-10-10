package server.game.docker.net.modules.pdus;

import server.game.docker.net.parents.pdus.PDU;

import java.util.Collection;
import java.util.Collections;

public class LobbyUpdate implements PDU {
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
    private int stateFlag;
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

    public void setStateFlag(int stateFlag) {
        this.stateFlag = stateFlag;
    }
}
