package server.game.docker.net.pdu;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.SocketAddress;

/**
 * A POJO DTO class that represents a high level PDU (Protocol Data Unit) for easier processing.
 */
public class PDU {
    private PDUType PDUType;
    /**
     * A byte value that represents the nature of this Packet
     */
//    @Deprecated
//    private final byte packetID;
    /**
     * Data transferred by this Packet at any given time
     */
//    @Deprecated
//    private final byte [] byteBuffer;
    /**
     * The IP address from which this packet was sent
     */
    private final SocketAddress address/* = null*/;
    /**
     * The port number from which this packet was sent
     */
    private final Integer port/* = -1*/;
    private Object data; //todo: will become generic type no - implementor will define attributes
    private ByteBuf byteBuf;

    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    //    public byte getPacketID() {
//        return packetID;
//    }
//
//    public void setAddress(InetAddress address) {
//        this.address = address;
//    }
//
//    public void setPort(Integer port) {
//        this.port = port;
//    }
//
    public SocketAddress getAddress() {
        return address;
    }
//
    public Integer getPort() {
        return port;
    }
//
//    public byte[] getByteBuffer() {
//        return byteBuffer;
//    }

//    public GameDataPDU() {
//        gameDataPDUType = GameDataPDUType.INVALID;
//        packetID = GameDataPDUType.INVALID.getID();
//        byteBuffer = new byte[0];
//    }
//
//    public GameDataPDU(final Byte packetID){
//        final byte [] encodedPacketID = packetID < 10? "0".concat(Byte.toString(packetID)).getBytes() : Byte.toString(packetID).getBytes();
//        this.packetID = packetID;
//        byteBuffer = encodedPacketID;
//    }
//
//    public GameDataPDU(final Byte packetID, String ... data){
//        this.packetID = packetID;
//        byteBuffer = Arrays.stream(data).reduce("", (part, dataN) -> part.isEmpty()? (packetID < 10? "0".concat(Byte.toString(packetID)) : Byte.toString(packetID)).concat(dataN) : part.concat(",").concat(dataN)).getBytes();
//    }
//
//    public GameDataPDU(final Byte packetID, byte [] byteBuffer){
//        this.packetID = packetID;
//        this.byteBuffer = byteBuffer;
//    }
//
    public PDU(final PDUType type, SocketAddress socketAddress, Integer port, Object data){ //todo: add IP and port
        this.PDUType = type;
//        this.byteBuffer = new byte[0];
//        this.packetID = type.getID();
        this.address = socketAddress;
        this.port = port;
        this.data = data;
        byteBuf = null;
    }

//    @Deprecated
//    public Vector<String> decode(){
//        return new Vector<>(
//            List.of(
//                new String(byteBuffer)
//                .trim()
//                .substring(2)
//                .split(",")
//        ));
//    }

//    public GameDataPDU withIPAndPort(final InetAddress address, final Integer port){
//        this.address = address;
//        this.port = port;
//        return this;
//    }
//
//    public static GameDataPDU fromDisconnectData(Long clientID){
//            return new GameDataPDU(GameDataPDUType.DISCONNECT.getID(), clientID.toString());
//    }
//
//    public static GameDataPDU fromWorldInfoData(Byte i, Byte j, Byte structureByte){
//        return new GameDataPDU(GameDataPDUType.WORLDINFO.getID(), Byte.toString(i), Byte.toString(j), structureByte.toString());
//    }
//
//    public static GameDataPDU fromPlayerMoveData(Long clientID, Byte i, Byte j, Byte structureByte){
//        return new GameDataPDU(GameDataPDUType.PLAYERMOVE.getID(), clientID.toString(), Byte.toString(i), Byte.toString(j), structureByte.toString());
//    }
//
    public PDUType getGameDataPDUType() {
        return PDUType;
    }

    public Object getData() {
        return data;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
