package server.game.docker.server.matchmaking.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDU00Join;
import server.game.docker.net.MyPDU01Disconnect;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.example.game.logic.SimpleRTSGameServerSideLogic;
import server.game.docker.server.matchmaking.session.models.GameSessionClient;

public class GameSession {
    //Config
    /**
     * The server port
     */
    // public static final Integer port = 4321; 
    /**
     * The server IP address
     */
    // public static final String iP = "127.0.0.1";
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
    private final Long gameSessionID = -1L;
    private final MyPDUActionHandler actionRouter;

    public GameSession(String [] args){// todo: rename to GameSession
        connectedClients = new Vector<>();
        actionRouter = new MyPDUActionHandler()
            .withActionEntry(MyPDUTypes.INVALID.getPacketID(), p -> System.out.println("Invalid packet received"))
            .withActionEntry(MyPDUTypes.JOIN.getPacketID(), p -> {
                // Vector<String> packetData = p.decode();
                System.out.println(String.format("A player (%s / %s) has joined the session", /*packetData.get(0)*/"Player " + (connectedClients.size() + 1), p.getAddress()));
                addConnection(new GameSessionClient(p.getAddress(), p.getPort(), /*packetData.get(0)*/Long.valueOf((long) connectedClients.size() + 1)), new MyPDU00Join(/*packetData.get(0)*/"Player " + /*Long.valueOf((long) */(connectedClients.size() + 1)), Long.valueOf((long) connectedClients.size() + 1)/*)*/);
                MyPDU playerIDInfoPacket = new MyPDU((byte) 05, Integer.toString(connectedClients.size()/* - 1*/));
                playerIDInfoPacket.setAddress(p.getAddress());
                playerIDInfoPacket.setPort(p.getPort());
                sendUnicast(playerIDInfoPacket);
            })
            .withActionEntry(MyPDUTypes.DISCONNECT.getPacketID(), p -> {
                // Packet01Disconnect disconnectPacket = new Packet01Disconnect(data);
                Vector<String> packetData = p.decode();
                System.out.println(String.format("A player (%s / %s) has left the session", /*packetData.get(0)*/"Player" + connectedClients.stream().map(GameSessionClient::getClientID).filter(Long.valueOf(packetData.get(0))::equals).findAny().orElse(-1L), p.getAddress()));
                sendMulticast(p);
                connectedClients.remove(connectedClients.indexOf(connectedClients.stream().filter(c -> Long.valueOf(packetData.get(0)).equals(c.getClientID())).findAny().orElse(null)));
                // sendMulticast(p/*.getByteBuffer()*/);
                System.out.println("A player has forfeit, terminating session ...");
                System.exit(0);
            });
        new SimpleRTSGameServerSideLogic(this).injectPacketActionHandler(actionRouter);
        try {
            socket = new DatagramSocket(4321);
        } catch (SocketException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        new Thread(){
            @Override 
            public void run(){
                serve(/*new SimpleRTSGameServerSideLogic(this)*/);
            }
        }.start();
        System.out.println("Server has started");
    }
    
    @Deprecated
    public void init(String[] args) {
        System.out.println("Server has started");
        // GameSession gameSession = new GameSession();

        new Thread(){
            @Override 
            public void run(){
                // gameSession.serve(new SimpleRTSGameServerSideLogic(gameSession));
            }
        }.start();

        // new SimpleRTSGameServerSideLogic(gameSession);

        // List<Socket> connectedClients = new ArrayList<>();

        //Initiate a socket that is awaiting client connections
        // try(ServerSocket serverSocket = new ServerSocket(port)){
        //     Long timeout = Instant.now().toEpochMilli() + 30000;
        //     while(!connectedClients.isEmpty() || timeout >= Instant.now().toEpochMilli()){
                //Accept incoming connections
        //         Socket socket = serverSocket.accept();
        //         connectedClients.add(socket);
        //         System.out.println(String.format("A client has connected: %s", socket.getLocalAddress()));
        //         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //         System.out.println(String.format("Client says: %s", bufferedReader.readLine()));
        //         PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        //         printWriter.println("Hello from server");
        //         socket.close();
        //         connectedClients.remove(socket);
        //         System.out.println("Client socket closed");
        //         timeout = Instant.now().toEpochMilli() + 30000;
        //     }
        // }
        // catch(IOException e){
        //     e.printStackTrace();
        // }
    }

    private void serve(/*SimpleRTSGameServerSideLogic gameLogic*/){
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } 
            catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            //Deserialize packet
            // gameLogic.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
            actionRouter.handle(packet);

            //Decode client message
            // String clientMessage = new String(packet.getData()).trim();

            //Log
            // System.out.println(String.format("Client sent: %s", clientMessage));

            //Server game service
            // if(/*new String(packet.getData().toString())*/clientMessage.equals("koupit"))
            //     sendData("Zlaty dul".getBytes(), packet.getAddress(), packet.getPort());
        }
    }

    private void addConnection(GameSessionClient gameClientPlayer, MyPDU00Join joinPacket, Long clientID){
        // if(gameClientPlayer.getID().equals(joinPacket)) //todo: base on "client side" connectionID;
        if(connectedClients.stream().map(GameSessionClient::getClientID).noneMatch(Long.valueOf(/*joinPacket.decode().get(0)*/clientID)::equals/*.getUsername()::equals*/)){
            connectedClients.add(gameClientPlayer);
            // sendToAll(joinPacket.getData());
            connectedClients.stream().filter(c -> !c.getClientID().equals(gameClientPlayer.getClientID())).forEach(c -> {
                joinPacket.setAddress(c.getiPAddress());
                joinPacket.setPort(c.getPort()); //todo: PakcetActionRouter will handle (source only)
                sendUnicast(/*joinPacket.getData(), c.getiPAddress(), c.getPort()*/joinPacket);
            });
        }
    }

    public void sendUnicast(/*byte[] data, InetAddress senderIP, Integer port*/MyPDU sourcePacket){
        DatagramPacket destinationPacket = new DatagramPacket(/*data, data.length, senderIP, port*/sourcePacket.getByteBuffer(), sourcePacket.getByteBuffer().length, sourcePacket.getAddress(), sourcePacket.getPort());
        try {
            socket.send(destinationPacket);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void sendMulticast(/*byte[] data*/MyPDU sourcePacket) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'sendToAll'");
        connectedClients.forEach(c -> {
            MyPDU destPacket = new MyPDU(sourcePacket.getPacketID(), sourcePacket.getByteBuffer());
            destPacket.setAddress(c.getiPAddress());
            destPacket.setPort(c.getPort());
            sendUnicast(/*datasourcePacket.getByteBuffer(), c.getiPAddress(), c.getPort()*/destPacket);
        });
    }

    public void endSession(String winnerName){
        System.out.println(String.format("%s has won", winnerName));
        System.out.println("Session end, terminating ...");
        System.exit(0);
    }
}