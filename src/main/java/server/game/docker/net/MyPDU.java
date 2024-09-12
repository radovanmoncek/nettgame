package server.game.docker.net;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

// import server.game.docker.client.GameClient;
// import server.game.docker.server.DockerGameServer;

public /*abstract*/ class MyPDU {
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
        //todo: Packet must be ID under 99, else throw IligiblePacketIDException - no
        final byte [] encodedPakcetID = packetID < 10? "0".concat(Byte.toString(packetID)).getBytes() : Byte.toString(packetID).getBytes();
        this.packetID = packetID.byteValue();
        byteBuffer = encodedPakcetID/*new byte[]{packetID}*/;
    }

    public MyPDU(final Byte packetID, String ... data){
        this.packetID = packetID;
        byteBuffer = Arrays.stream(data).reduce("", (part, dataN) -> part.isEmpty()? /*Byte.toString(packetID)*/(packetID < 10? "0".concat(Byte.toString(packetID)) : Byte.toString(packetID)).concat(dataN) : part.concat(",").concat(dataN)).getBytes();
    }

    public MyPDU(final Byte packetID, byte [] byteBuffer){
        this.packetID = packetID;
        this.byteBuffer = byteBuffer;
    }

    // public Packet(){

    // }

    public Vector<String> decode(){
        return new Vector<>(
            List.of(
                new String(byteBuffer)
                .trim()
                .substring(2)
                .split(",")
        ));
    }

    //todo: Pakcet shall be a generic class; used for generic communication
    // Unicast data send
    // public abstract void writeData(GameClient gameClient);
    // Broadcast data send
    // public abstract void writeData(DockerGameServer dockerGameServer);
    // @Deprecated
    // public String readData(byte[] data){
    //     //todo: serialization; milestone
    //     //Packet sanitization; omit ID bytes
    //     return new String(data).trim().substring(2);
    // }

    // @Deprecated
    // public /*abstract*/ byte [] getData(){return new byte [0];};

    //todo: PacketRouter
    // @Deprecated
    // public static MyPDUTypes lookupPacket(Integer id){
    //     return Stream.of(MyPDUTypes.values()).filter(p -> p.packetID.equals(id)).findAny().orElse(MyPDUTypes.INVALID);
    // }

    // @Deprecated
    // public static MyPDUTypes lookupPacket(String packetID) {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'lookupPacket'");
    //     try {
    //         return lookupPacket(Integer.parseInt(packetID));
    //     } catch (NumberFormatException e) {
    //         // TODO: handle exception
    //         e.printStackTrace();
    //         return MyPDUTypes.INVALID;
    //     }
    // }

    // private void checkPacketIDEligibility(Byte packetID) throws IneligiblePacketIDException {
    //     if(packetID > 99) throw new IneligiblePacketIDException();
    // }

    // public static class IneligiblePacketIDException extends Exception {
    // }
}
