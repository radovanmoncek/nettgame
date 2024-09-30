package server.game.docker.net.pdu;

import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;

/**
 * A POJO DTO class that represents a high level representation of a PDU (Protocol Data Unit) for easier processing.
 */
public class PDU {
    /**
     * The {@link PDUType} of this PDU POJO DTO.
     */
    private PDUType PDUType;
    /**
     * The IP address from which this PDU was sent
     */
    private SocketAddress address;
    /**
     * The highest level abstraction of the data transferred by this PDU POJO-DTO
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
}
