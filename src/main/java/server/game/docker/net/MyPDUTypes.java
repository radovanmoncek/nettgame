package server.game.docker.net;

public enum MyPDUTypes {
    INVALID((byte) -1), JOIN((byte) 00), DISCONNECT((byte) 01), WORLDINFO((byte) 02), PLAYERMOVE((byte) 03);

    Byte packetID;

    private MyPDUTypes(Byte packetID){
        this.packetID = packetID;
    }
    public Byte getPacketID() {
        return packetID;
    }
}