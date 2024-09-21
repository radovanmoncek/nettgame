package server.game.docker.net.pdu;

import server.game.docker.net.LocalPipeline;

import java.util.stream.Stream;

/**
 * <p>
 * This enumeration represents all the possible prefixes that can mark a given {@link PDU}, and thus its mapped {@link LocalPipeline} also.
 * </p>
 * <p>
 * All actions necessary for a successful networked game implementation should be present (it is supplied as-is); therefore it is not recommended for a DockerGameServer and Client implementor to in any way modify this enumeration as
 * the behaviour of such modifications is not defined.
 * </p>
 */
public enum PDUType {
    //SPECIAL
    INVALID((byte) -1, false),
    //ID
    IDRES((byte) 11, (short) /*1*/Long.BYTES),
    //LOBBY
    CREATELOBBYREQ((byte) 12, false), CREATELOBBYRES((byte) 13, (short) /*4*/Long.BYTES), JOINLOBBYREQ((byte) 16, false), JOINLOBBYRES((byte) 17, false), LEAVELOBBYREQ((byte) 14, false), LEAVELOBBYRES((byte) 15, false),

    JOIN((byte) 0, false), DISCONNECT((byte) 1, false), WORLDINFO((byte) 2, true), PLAYERMOVE((byte) 3, true), GAMESTART((byte) 4, false), /*IDREQUEST((byte) 5, false),*/ GAMEEND((byte) 6, false), SERVERTICKUPDATE((byte) 7, true), CHATMESSAGE((byte) 10, true);

    private final Byte iD;
    private final Short minimumTransportSize;
    /**
     * <p>
     *     {@link Short} minimumTransportSize will be read from a special PDU header value.
     * </p>
     * <p>
     *     For e.g.: PDU body: {}
     * </p>
     */
    private final boolean variableLen;

    PDUType(Byte iD, boolean variableLen) {
        this.iD = iD;
        //Do not expect any data payload in this PDU
        minimumTransportSize = /*2*/0;
        this.variableLen = variableLen;
    }

    PDUType(Byte iD, Short minimumTransportSize){
        this.iD = iD;
        this.minimumTransportSize = /*(short) (*/minimumTransportSize/* + 2)*/;
        variableLen = false;
    }

    public Byte getID() {
        return iD;
    }

    public Short getMinimumTransportSize() {
        return minimumTransportSize;
    }

    public static PDUType valueOf(Byte iD){
        return Stream.of(PDUType.values()).filter(v -> iD.equals(v.iD)).findAny().orElse(INVALID);
    }

    public boolean isVariableLen() {
        return variableLen;
    }
}