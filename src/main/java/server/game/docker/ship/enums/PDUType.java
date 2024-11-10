package server.game.docker.ship.enums;

import server.game.docker.GameServerInitializer;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.stream.Stream;

/**
 * <p>
 * This enumeration represents all the possible prefixes that can mark a given {@link PDU}, and thus its mapped {@link GameServerInitializer.RouterHandler} also.
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
    INVALID,
    /*--------ID--------*/
    USERNAME,
    /*--------LOBBY--------*/
    LOBBYREQUEST, LOBBYUPDATE, LOBBYBEACON,
    /*--------CHAT--------*/
    /**
     * Non-empty variable length PDU meant for reliable user chat message transportation.
     */
    CHATMESSAGE;

    /**
     * Whether to use reliable transport mechanisms for this {@link PDUType}.
     * @see #isTransportedReliably()
     */
    private final boolean transportedReliably;

    /**
     * <p>
     *     Constructs a new {@link PDUType} ENUM constant with {@link #oneBasedOrdinal() oneBasedOrdinal} as its unique identifier for over-the-wire transport.
     * </p>
     * @param transportedReliably whether to use reliable transport mechanisms for this {@link PDUType}
     */
    PDUType(boolean transportedReliably) {
        this.transportedReliably = transportedReliably;
    }

    PDUType() {
        this.transportedReliably = false;
    }

    public static PDUType valueOf(byte value) {
        return Stream.of(PDUType.values()).filter(v -> v.oneBasedOrdinal() == value).findAny().orElse(INVALID);
    }

    public boolean isTransportedReliably() {
        return transportedReliably;
    }

    /**
     * <p>
     *     It is <i>imperative</i> that this method is called instead of {@link #ordinal() ordinal} for byte array representation.
     * </p>
     * @return {@code int} {@code this.ordinal() + 1}
     */
    public int oneBasedOrdinal(){
        return ordinal() + 1;
    };
}