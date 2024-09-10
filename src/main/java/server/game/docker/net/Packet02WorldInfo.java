package server.game.docker.net;

public class Packet02WorldInfo extends Packet {
    private Byte i, j;
    public Byte generated;

    public Packet02WorldInfo(byte [] data) {
        super(02);
        String[] dataArray = readData(data).split(",");
        i = Byte.parseByte(dataArray[0]);
        j = Byte.parseByte(dataArray[1]);
        generated = Byte.parseByte(dataArray[2]);
        // i = readData(data); todo: maybe fog of war??
        //TODO Auto-generated constructor stub
    }

    // public Packet02WorldInfo(/*String username*/Byte i, Byte j){ one sided communication
    //     super(02);
    //     this.i = i;
    //     this.j = j;
    //     // this.i = username;
    // }

    @Override
    public byte[] getData() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getData'");
        return "02".concat(Byte.toString(i)).concat(",").concat(Byte.toString(j)).concat(",").concat(Byte.toString(generated)).getBytes();
    }

    public Byte getI() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
        return i;
    }

    public Byte getJ() {
        return j;
    }
}
