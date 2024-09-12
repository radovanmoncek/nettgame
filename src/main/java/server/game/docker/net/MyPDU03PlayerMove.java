package server.game.docker.net;

public class MyPDU03PlayerMove extends MyPDU {
    public MyPDU03PlayerMove(String moveType, String moveData){
        super((byte) 03, moveType, moveData);
        packetType = MyPDUTypes.PLAYERMOVE;
    }

    public MyPDUTypes getPacketType(){
        return packetType;
    }
}
