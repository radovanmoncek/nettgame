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
     * The IP address from which this packet was sent
     */
    private SocketAddress address;
    /**
     * The port number from which this packet was sent
     */
    private Integer port;
    /**
     * The highest level abstraction of the decoded data transferred by this PDU POJO DTO
     */
    private Object data; //todo: will become generic type no - implementor will define attributes
    private ByteBuf byteBuf;

    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public PDU(final PDUType type, SocketAddress socketAddress, Integer port, Object data){ //todo: add IP and port
        this.PDUType = type;
        this.address = socketAddress;
        this.port = port;
        this.data = data;
        byteBuf = null;
    }

    public PDU(){

    }

    public PDUType getPDUType() {
        return PDUType;
    }

    public Object getData() {
        return data;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public void setPDUType(server.game.docker.net.pdu.PDUType PDUType) {
        this.PDUType = PDUType;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
