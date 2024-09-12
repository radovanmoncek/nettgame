package server.game.docker.net;

// import server.game.docker.client.GameClient;
// import server.game.docker.server.DockerGameServer;

public class MyPDU00Join extends MyPDU {
    @Deprecated
    private String username;

    // public Packet00Join(byte [] data) {
    //     super(00);
    //     username = readData(data);
    //     //TODO Auto-generated constructor stub
    // }
    
    public MyPDU00Join(/*String username*/) {
        super((byte) 00/*, username*/);
        // this.username = username;
    }

    public MyPDU00Join(String username){
        super((byte) 00, username);
    }
    
    // @Override
    // public void writeData(GameClient gameClient) {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'writeData'");
    //     gameClient.sendData(getData());
    // }

    // @Override
    // public void writeData(DockerGameServer dockerGameServer) {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'writeData'");
    //     dockerGameServer.sendToAll(getData());
    // }

    @Override
    public byte[] getData() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getData'");
        return "00".concat(username).getBytes();
    }

    @Deprecated
    public String getUsername() {
        return username;
    }
}
