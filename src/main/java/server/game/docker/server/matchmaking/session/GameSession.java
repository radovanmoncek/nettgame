package server.game.docker.server.matchmaking.session;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDUActionMapper;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.chat.SessionChat;
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
    private final MyPDUActionMapper actionMapper;

    public MyPDUActionMapper getActionMapper() {
        return actionMapper;
    }

    public GameSession(String [] args){
        gameSessionID = -1L;
        port = 4321;
        connectedClients = new Vector<>();
        actionMapper = new MyPDUActionMapper()
            .withActionMapping(MyPDUTypes.INVALID.getID(), p -> System.out.println("Invalid packet received"))
            .withActionMapping(MyPDUTypes.JOIN.getID(), p -> {
                System.out.printf("A player (%s / %s) has joined the session\n", "Player " + (connectedClients.size() + 1), p.getAddress());
                addConnection(new GameSessionClient(p.getAddress(), p.getPort(), (long) connectedClients.size() + 1), new MyPDU(MyPDUTypes.JOIN.getID(), "Player " + (connectedClients.size() + 1)), (long) connectedClients.size() + 1);
                sendUnicast(new MyPDU(MyPDUTypes.IDREQUEST.getID(), Integer.toString(connectedClients.size())).withIPAndPort(p.getAddress(), p.getPort()));
            })
            .withActionMapping(MyPDUTypes.DISCONNECT.getID(), p -> {
                Vector<String> packetData = p.decode();
                System.out.printf("A player (%s / %s) has left the session\n", "Player" + connectedClients.stream().map(GameSessionClient::getClientID).filter(Long.valueOf(packetData.get(0))::equals).findAny().orElse(-1L), p.getAddress());
                sendMulticast(p);
                connectedClients.remove(connectedClients.stream().filter(c -> Long.valueOf(packetData.get(0)).equals(c.getClientID())).findAny().orElse(null));
                System.out.println("A player has forfeit, terminating session ...");
                System.exit(0);
            });
        new SimpleRTSGameServerSideLogic(this);
        new SessionChat(this);
        try {
            socket = new DatagramSocket(4321);
        } catch (SocketException e) {
            e.printStackTrace();
            socket.close();
        }
        System.out.printf("Session ID: %d port: %d has started\n", gameSessionID, port);
        serve();
    }
    
    private void serve(){
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
                actionMapper.map(packet);
            } 
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addConnection (GameSessionClient gameClientPlayer, MyPDU joinPacket, Long clientID) {
        if(connectedClients.stream().map(GameSessionClient::getClientID).noneMatch(clientID::equals)) {
            connectedClients.add(gameClientPlayer);
            connectedClients.stream().filter(c -> !c.getClientID().equals(gameClientPlayer.getClientID())).forEach(c ->
                    sendUnicast(joinPacket.withIPAndPort(c.getClientIPAddress(), c.getPort()))
            );
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
            destPacket.setAddress(c.getClientIPAddress());
            destPacket.setPort(c.getPort());
            sendUnicast(destPacket);
        });
    }

    public void endSession(String winnerName){
        System.out.printf("%s has won", winnerName); //todo: should be announced by Game Logic
        System.out.println("Session end, terminating ...");
        System.exit(0);
    }
}