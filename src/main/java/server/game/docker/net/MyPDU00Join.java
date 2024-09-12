package server.game.docker.net;

public class MyPDU00Join extends MyPDU {
    
    public MyPDU00Join() {
        super((byte) 00);
    }

    public MyPDU00Join(String username){
        super((byte) 00, username);
    }
}
