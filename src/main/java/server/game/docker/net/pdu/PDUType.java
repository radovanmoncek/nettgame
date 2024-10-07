package server.game.docker.net.pdu;

import server.game.docker.net.LocalPDUPipeline;
import server.game.docker.net.encoders.GameEncoder;

import java.util.stream.Stream;

/**
 * <p>
 * This enumeration represents all the possible prefixes that can mark a given {@link PDU}, and thus its mapped {@link LocalPDUPipeline} also.
 * </p>
 * <p>
 * All actions necessary for a successful networked game implementation should be present (it is supplied as-is); therefore, it is not recommended for a DockerGameServer and Client implementor to in any way modify this enumeration as
 * the behaviour of such modifications is not defined.
 * </p>
 */
public enum PDUType {
    /*--------SPECIAL--------*/
    /**
     * Empty fixed length PDU with id {@code -1} meant to signal that this PDU is invalid and not fit for any processing.
     */
    INVALID((byte) -1, true, false),
    /*--------ID--------*/
    IDRES((byte) 1, true, true),
    /*--------LOBBY--------*/
    CREATELOBBYREQ((byte) 2, true, true), CREATELOBBYRES((byte) 3, false, true), JOINLOBBYREQ((byte) 4, false, true), JOINLOBBYRES((byte) 5, false, true), LEAVELOBBYREQ((byte) 6, true, true), LEAVELOBBYRES((byte) 7, false, true), LOBBYBEACON((byte) 8, false, true),
    /*--------CHAT--------*/
    /**
     * Non-empty variable length PDU with id {@code 9} meant for reliable user chat message transportation.
     */
    CHATMESSAGE((byte) 9, false, true);

    private final Byte iD;
    /**
     * <p>
     *     PDU size will be read from a special PDU header value, which will be injected in {@link GameEncoder}.
     * </p>
     * <p>
     *     For e.g.: PDU body: {}
     * </p>
     */
    private final boolean empty;
    private final boolean transportedReliably;

    /**
     *
     * @param iD the ID which will be assigned to this {@link PDUType}
     * @param empty Do not expect any data payload in this PDU
     * @param transportedReliably whether to use reliable transport mechanisms for this {@link PDUType}
     */
    PDUType(Byte iD, boolean empty, boolean transportedReliably) {
        this.iD = iD;
        this.empty = empty;
        this.transportedReliably = transportedReliably;
    }

    public Byte getID() {
        return iD;
    }

    public static PDUType valueOf(Byte iD){
        return Stream.of(PDUType.values()).filter(v -> iD.equals(v.iD)).findAny().orElse(INVALID);
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isTransportedReliably() {
        return transportedReliably;
    }
}