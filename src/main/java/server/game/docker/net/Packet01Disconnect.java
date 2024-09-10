package server.game.docker.net;

import java.lang.reflect.Array;

public class Packet01Disconnect extends Packet {
    private String username;

    public Packet01Disconnect(byte [] data) {
        super(01);
        username = readData(data);
        //TODO Auto-generated constructor stub
    }

    public Packet01Disconnect(String username){
        super(01);
        this.username = username;
    }

    @Override
    public byte[] getData() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getData'");
        return "01".concat(username).getBytes();
    }

    public Object getUsername() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
        return username;
    }

}
