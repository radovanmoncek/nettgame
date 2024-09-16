package server.game.docker.net;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class MyPDUActionMapper {
    private final Map<Byte, MyPDUAction> packetActionMappings;

    public MyPDUActionMapper() {
        this.packetActionMappings = new HashMap<>();
    }

    public MyPDUActionMapper withActionMapping(Byte packetID, MyPDUAction packetAction){
        packetActionMappings.put(packetID, packetAction);
        return this;
    }

    public void map(DatagramPacket packetToMap){
        MyPDU routedPacket = new MyPDU(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2)), packetToMap.getData());
        routedPacket.setAddress(packetToMap.getAddress());
        routedPacket.setPort(packetToMap.getPort());
        packetActionMappings.get(Byte.parseByte(new String(packetToMap.getData()).substring(0, 2))).perform(routedPacket);
    }

}
