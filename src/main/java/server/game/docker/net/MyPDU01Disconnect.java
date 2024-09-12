package server.game.docker.net;

public class MyPDU01Disconnect extends MyPDU {

    public MyPDU01Disconnect(Long clientID){
        super((byte) 01, clientID.toString());
    }
}
