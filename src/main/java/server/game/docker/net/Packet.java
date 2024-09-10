package server.game.docker.net;

import java.util.stream.Stream;

// import server.game.docker.client.GameClient;
// import server.game.docker.server.DockerGameServer;

public abstract class Packet {

    public static enum PacketTypes {
        INVALID(-1), JOIN(00), DISCONNECT(01), WORLDINFO(02);

        private Integer packetID;

        private PacketTypes(Integer packetID){
            this.packetID = packetID;
        }
        public Integer getPacketID() {
            return packetID;
        }
    }

    public byte packetID;

    public Packet(Integer packetID){
        this.packetID = packetID.byteValue();
    }

    //todo: Pakcet shall be generic class; used for generic communication
    // Unicast data send
    // public abstract void writeData(GameClient gameClient);
    // Broadcast data send
    // public abstract void writeData(DockerGameServer dockerGameServer);

    public String readData(byte[] data){
        //todo: serialization; milestone
        //Packet sanitization; omit ID bytes
        return new String(data).trim().substring(2);
    }

    public abstract byte [] getData();

    //todo: PacketRouter
    public static PacketTypes lookupPacket(Integer id){
        return Stream.of(PacketTypes.values()).filter(p -> p.packetID.equals(id)).findAny().orElse(PacketTypes.INVALID);
    }

    public static PacketTypes lookupPacket(String packetID) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'lookupPacket'");
        try {
            return lookupPacket(Integer.parseInt(packetID));
        } catch (NumberFormatException e) {
            // TODO: handle exception
            e.printStackTrace();
            return PacketTypes.INVALID;
        }
    }
}
