package server.game.docker.net;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MyPDU {
    protected MyPDUTypes packetType;
    /**
     * A byte value that represents the nature of this Packet
     */
    protected final byte packetID;
    /**
     * Data transfered by this Packet at any given time
     */
    protected final byte [] byteBuffer;
    /**
     * The IP address from which this packet was sent
     */
    protected InetAddress address = null;
    /**
     * The port number from which this packet was sent
     */
    protected Integer port = -1;

    public byte getPacketID() {
        return packetID;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public byte[] getByteBuffer() {
        return byteBuffer;
    }

    public MyPDU(final Byte packetID){
        final byte [] encodedPakcetID = packetID < 10? "0".concat(Byte.toString(packetID)).getBytes() : Byte.toString(packetID).getBytes();
        this.packetID = packetID.byteValue();
        byteBuffer = encodedPakcetID;
    }

    public MyPDU(final Byte packetID, String ... data){
        this.packetID = packetID;
        byteBuffer = Arrays.stream(data).reduce("", (part, dataN) -> part.isEmpty()? (packetID < 10? "0".concat(Byte.toString(packetID)) : Byte.toString(packetID)).concat(dataN) : part.concat(",").concat(dataN)).getBytes();
    }

    public MyPDU(final Byte packetID, byte [] byteBuffer){
        this.packetID = packetID;
        this.byteBuffer = byteBuffer;
    }

    public Vector<String> decode(){
        return new Vector<>(
            List.of(
                new String(byteBuffer)
                .trim()
                .substring(2)
                .split(",")
        ));
    }
}
