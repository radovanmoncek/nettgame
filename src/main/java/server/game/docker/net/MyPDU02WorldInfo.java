package server.game.docker.net;

public class MyPDU02WorldInfo extends MyPDU {
    // private Byte i, j;
    // public Byte generated;

    // public Packet02WorldInfo(byte [] data) {
    //     super(02);
    //     String[] dataArray = readData(data).split(",");
    //     i = Byte.parseByte(dataArray[0]);
    //     j = Byte.parseByte(dataArray[1]);
    //     generated = Byte.parseByte(dataArray[2]);
    //     // i = readData(data); todo: maybe fog of war??
    //     //TODO Auto-generated constructor stub
    // }

    // public Packet02WorldInfo(/*String username*/Byte i, Byte j){ one sided communication
    //     super(02);
    //     this.i = i;
    //     this.j = j;
    //     // this.i = username;
    // }

    // @Deprecated
    // public Packet02WorldInfo(Byte i, Byte j, Byte generated){
    //     super((byte) 02, Byte.toString(i), Byte.toString(j), Byte.toString(generated));
    // };

    public MyPDU02WorldInfo(Byte i, Byte j, /*String playerName*/Byte structureByte){
        super((byte) 02, Byte.toString(i), Byte.toString(j), /*playerName*/structureByte.toString());
    }

    // @Override
    // @Deprecated
    // public byte[] getData() {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'getData'");
    //     return "02"/*.concat(Byte.toString(i)).concat(",").concat(Byte.toString(j)).concat(",").concat(Byte.toString(generated(byte) 0))*/.getBytes();
    // }

    // public Byte getI() {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
    //     return i;
    // }

    // public Byte getJ() {
    //     return j;
    // }
}
