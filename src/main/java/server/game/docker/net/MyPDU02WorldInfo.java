package server.game.docker.net;

public class MyPDU02WorldInfo extends MyPDU {

    public MyPDU02WorldInfo(Byte i, Byte j, Byte structureByte){
        super((byte) 02, Byte.toString(i), Byte.toString(j), structureByte.toString());
    }
}
