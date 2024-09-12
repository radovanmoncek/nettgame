package server.game.docker.net;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MyPDUActionHandler {
    private final Map<Byte, MyPDUAction> packetActionRoutes;

    public MyPDUActionHandler() {
        this.packetActionRoutes = new HashMap<>();
    }

    public MyPDUActionHandler withActionEntry(Byte packetID, MyPDUAction packetAction){
        packetActionRoutes.put(packetID, packetAction);
        return this;
    }

    public void handle(DatagramPacket packetToRoute){
        MyPDU routedPacket = new MyPDU(Byte.parseByte(new String(packetToRoute.getData()).substring(0, 2)), packetToRoute.getData());
        routedPacket.setAddress(packetToRoute.getAddress());
        routedPacket.setPort(packetToRoute.getPort());
        packetActionRoutes.get(Byte.parseByte(new String(packetToRoute.getData()).substring(0, 2))).perform(routedPacket);
    }

    @FunctionalInterface
    public static interface MyPDUAction {
        void perform(MyPDU packet);
    }
}
