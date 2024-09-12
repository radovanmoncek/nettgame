package server.game.docker.net;

import java.lang.reflect.Array;

public class MyPDU01Disconnect extends MyPDU {
    // private String username;

    // public Packet01Disconnect(byte [] data) {
    //     super(01);
    //     username = readData(data);
    //     //TODO Auto-generated constructor stub
    // }

    public MyPDU01Disconnect(/*String username*/Long clientID){
        super((byte) 01, /*username*/clientID.toString());
        // this.username = username;
    }

    @Override
    public byte[] getData() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getData'");
        return "01"/*.concat(username)*/.getBytes();
    }

    // @Deprecated
    // public Object getUsername() {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
    //     return username;
    // }

}
