package server.game.docker.net.pdu;

import server.game.docker.net.LocalPipeline;
import server.game.docker.net.encoders.GameEncoder;

import java.util.stream.Stream;

/**
 * <p>
 * This enumeration represents all the possible prefixes that can mark a given {@link PDU}, and thus its mapped {@link LocalPipeline} also.
 * </p>
 * <p>
 * All actions necessary for a successful networked game implementation should be present (it is supplied as-is); therefore, it is not recommended for a DockerGameServer and Client implementor to in any way modify this enumeration as
 * the behaviour of such modifications is not defined.
 * </p>
 */
public enum PDUType {
    //SPECIAL
    INVALID((byte) -1, false),
    //ID
    IDRES((byte) 11,/* (short) *//*1*//*Long.BYTES*/ true),
    //LOBBY
    CREATELOBBYREQ((byte) 12, false), CREATELOBBYRES((byte) 13, (short) /*4*/Long.BYTES), JOINLOBBYREQ((byte) 16, false), JOINLOBBYRES((byte) 17, false), LEAVELOBBYREQ((byte) 14, false), LEAVELOBBYRES((byte) 15, false), LOBBYBEACON((byte) 18, false),
    //CHAT

    JOIN((byte) 0, false), DISCONNECT((byte) 1, false), WORLDINFO((byte) 2, true), PLAYERMOVE((byte) 3, true), GAMESTART((byte) 4, false), /*IDREQUEST((byte) 5, false),*/ GAMEEND((byte) 6, false), SERVERTICKUPDATE((byte) 7, true), CHATMESSAGE((byte) 10, true);

    private final Byte iD;
//    private final Short minimumTransportSize;
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

    PDUType(Byte iD, boolean empty) {
        this.iD = iD;
        //Do not expect any data payload in this PDU
//        minimumTransportSize = /*2*/0;
        this.empty = empty;
        this.transportedReliably = false;
    }

    PDUType(Byte iD, Short minimumTransportSize){
        this.iD = iD;
//        this.minimumTransportSize = /*(short) (*/minimumTransportSize/* + 2)*/;
        empty = false;
        this.transportedReliably = false;
    }

    public Byte getID() {
        return iD;
    }

    public Short getMinimumTransportSize() {
        return /*minimumTransportSize*/0;
    }

    public static PDUType valueOf(Byte iD){
        return Stream.of(PDUType.values()).filter(v -> iD.equals(v.iD)).findAny().orElse(INVALID);
    }

    public boolean isEmpty() {
        return /*variableLen*/false;
    }
}