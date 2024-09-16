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

    public MyPDU() {
        packetType = MyPDUTypes.INVALID;
        packetID = MyPDUTypes.INVALID.getID();
        byteBuffer = new byte[0];
    }

    public MyPDU(final Byte packetID){
        final byte [] encodedPacketID = packetID < 10? "0".concat(Byte.toString(packetID)).getBytes() : Byte.toString(packetID).getBytes();
        this.packetID = packetID;
        byteBuffer = encodedPacketID;
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

    public MyPDU withIPAndPort(final InetAddress address, final Integer port){
        this.address = address;
        this.port = port;
        return this;
    }

    public static MyPDU fromDisconnectData(Long clientID){
            return new MyPDU(MyPDUTypes.DISCONNECT.getID(), clientID.toString());
    }

    public static MyPDU fromWorldInfoData(Byte i, Byte j, Byte structureByte){
        return new MyPDU(MyPDUTypes.WORLDINFO.getID(), Byte.toString(i), Byte.toString(j), structureByte.toString());
    }

    public static MyPDU fromPlayerMoveData(Long clientID, Byte i, Byte j, Byte structureByte){
        return new MyPDU(MyPDUTypes.PLAYERMOVE.getID(), clientID.toString(), Byte.toString(i), Byte.toString(j), structureByte.toString());
    }
}
