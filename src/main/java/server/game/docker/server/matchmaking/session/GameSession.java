package server.game.docker.server.matchmaking.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDU00Join;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.examples.game.logic.SimpleRTSGameServerSideLogic;
import server.game.docker.server.matchmaking.session.models.GameSessionClient;

public class GameSession {
    //Config
    /**
     * The server port
     */
    public final Integer port; 
    /**
     * The server socket
     */
    public DatagramSocket socket;
    /**
     * The list of connected clients
     */
    public final List<GameSessionClient> connectedClients;
    /**
     * The ID of this game session
     */
    private final Long gameSessionID;
    private final MyPDUActionHandler actionRouter;

    public GameSession(String [] args){
        gameSessionID = -1L;
        port = 4321;
        connectedClients = new Vector<>();
        actionRouter = new MyPDUActionHandler()
            .withActionEntry(MyPDUTypes.INVALID.getPacketID(), p -> System.out.println("Invalid packet received"))
            .withActionEntry(MyPDUTypes.JOIN.getPacketID(), p -> {
                System.out.println(String.format("A player (%s / %s) has joined the session", "Player " + (connectedClients.size() + 1), p.getAddress()));
                addConnection(new GameSessionClient(p.getAddress(), p.getPort(), Long.valueOf((long) connectedClients.size() + 1)), new MyPDU00Join("Player " + (connectedClients.size() + 1)), Long.valueOf((long) connectedClients.size() + 1));
                MyPDU playerIDInfoPacket = new MyPDU((byte) 05, Integer.toString(connectedClients.size()));
                playerIDInfoPacket.setAddress(p.getAddress());
                playerIDInfoPacket.setPort(p.getPort());
                sendUnicast(playerIDInfoPacket);
            })
            .withActionEntry(MyPDUTypes.DISCONNECT.getPacketID(), p -> {
                Vector<String> packetData = p.decode();
                System.out.println(String.format("A player (%s / %s) has left the session", "Player" + connectedClients.stream().map(GameSessionClient::getClientID).filter(Long.valueOf(packetData.get(0))::equals).findAny().orElse(-1L), p.getAddress()));
                sendMulticast(p);
                connectedClients.remove(connectedClients.indexOf(connectedClients.stream().filter(c -> Long.valueOf(packetData.get(0)).equals(c.getClientID())).findAny().orElse(null)));
                System.out.println("A player has forfeit, terminating session ...");
                System.exit(0);
            });
        new SimpleRTSGameServerSideLogic(this).injectPacketActionHandler(actionRouter);
        try {
            socket = new DatagramSocket(4321);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        serve();
        System.out.println(String.format("Session ID: %d port: %d has started", gameSessionID, port));
    }
    
    private void serve(){
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            actionRouter.handle(packet);
        }
    }

    private void addConnection(GameSessionClient gameClientPlayer, MyPDU00Join joinPacket, Long clientID){
        if(connectedClients.stream().map(GameSessionClient::getClientID).noneMatch(Long.valueOf(clientID)::equals)){
            connectedClients.add(gameClientPlayer);
            connectedClients.stream().filter(c -> !c.getClientID().equals(gameClientPlayer.getClientID())).forEach(c -> {
                joinPacket.setAddress(c.getiPAddress());
                joinPacket.setPort(c.getPort());
                sendUnicast(joinPacket);
            });
        }
    }

    public void sendUnicast(MyPDU sourcePacket){
        DatagramPacket destinationPacket = new DatagramPacket(sourcePacket.getByteBuffer(), sourcePacket.getByteBuffer().length, sourcePacket.getAddress(), sourcePacket.getPort());
        try {
            socket.send(destinationPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMulticast(MyPDU sourcePacket) {
        connectedClients.forEach(c -> {
            MyPDU destPacket = new MyPDU(sourcePacket.getPacketID(), sourcePacket.getByteBuffer());
            destPacket.setAddress(c.getiPAddress());
            destPacket.setPort(c.getPort());
            sendUnicast(destPacket);
        });
    }

    public void endSession(String winnerName){
        System.out.println(String.format("%s has won", winnerName));
        System.out.println("Session end, terminating ...");
        System.exit(0);
    }
}