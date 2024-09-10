package server.game.docker.net;

// import server.game.docker.client.GameClient;
// import server.game.docker.server.DockerGameServer;

public class Packet00Join extends Packet {
    private String username;

    public Packet00Join(byte [] data) {
        super(00);
        username = readData(data);
        //TODO Auto-generated constructor stub
    }
    
    public Packet00Join(String username) {
        super(00);
        this.username = username;
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

    public String getUsername() {
        return username;
    }
}
