package server.game.docker.net;

public enum MyPDUTypes {
    INVALID((byte) -1), JOIN((byte) 0), DISCONNECT((byte) 1), WORLDINFO((byte) 2), PLAYERMOVE((byte) 3), GAMESTART((byte) 4), IDREQUEST((byte) 5), GAMEEND((byte) 6), SERVERTICKUPDATE((byte) 7), CHATMESSAGE((byte) 10);

    private final Byte iD;

    MyPDUTypes(Byte iD){
        this.iD = iD;
    }

    public Byte getID() {
        return iD;
    }
}