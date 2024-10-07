package server.game.docker.net.pdu;

import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;
import java.util.Arrays;

/**
 * PDU:
 * <pre>
 *     ---------------------------------------------------
 *     |                 Protocol ID (3B)                |
 *     ---------------------------------------------------
 *     | PDUType byte (1B)     |    dataLength int? (4B) |
 *     ---------------------------------------------------
 *     |                  data (max length)              |
 *     ---------------------------------------------------
 * </pre>
 * A high level representation of a PDU (Protocol Data Unit) for easier processing.
 * Each field of the PDU diagram above directly maps to a field of this class.
 */
public final class PDU {
    /**
     * Immutable read-only field that denotes that this is a GameData PDU.
     */
    private static final byte [] protocolID = new byte[]{'F', 'E', 'D'};
    /**
     * The {@link PDUType} of this PDU.
     */
    private PDUType PDUType;
    /**
     * The IP address from which this PDU was sent
     */
    @Deprecated
    private SocketAddress address;
    /**
     * The length of the {@link #data data} being transferred by this {@link PDU} instance.
     */
    private Long dataLength;
    /**
     * The highest level abstraction of the data transferred by this {@link PDU}.
     */
    private Object data; //todo: will become generic type no - implementor will define attributes

    public SocketAddress getAddress() {
        return address;
    }

    public PDUType getPDUType() {
        return PDUType;
    }

    public Object getData() {
        return data;
    }

    public void setPDUType(server.game.docker.net.pdu.PDUType PDUType) {
        this.PDUType = PDUType;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public byte [] getProtocolID() {
        return Arrays.copyOf(protocolID, protocolID.length);
    }

    public Long getDataLength() {
        return dataLength;
    }

    public void setDataLength(Long dataLength) {
        this.dataLength = dataLength;
    }
}
