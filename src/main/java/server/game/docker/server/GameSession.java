package server.game.docker.server;

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

import server.game.docker.net.Packet;
import server.game.docker.net.Packet00Join;
import server.game.docker.net.Packet01Disconnect;
import server.game.docker.net.Packet.PacketTypes;

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
    public final List<GameClientPlayer> connectedClients;
    /**
     * The ID of this game session
     */
    private final Long gameSessionID = -1L;

    public GameSession(){// todo: rename to GameSession
        connectedClients = new Vector<>();
        try {
            socket = new DatagramSocket(4321);
        } catch (SocketException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Server has started");
        GameSession gameSession = new GameSession();

        new Thread(){
            @Override 
            public void run(){
                gameSession.serve(new SimpleRTSGameServerSideLogic(gameSession));
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

    public void serve(SimpleRTSGameServerSideLogic gameLogic){
        while(true){
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            //Deserialize packet
            gameLogic.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());

            //Decode client message
            // String clientMessage = new String(packet.getData()).trim();

            //Log
            // System.out.println(String.format("Client sent: %s", clientMessage));

            //Server game service
            // if(/*new String(packet.getData().toString())*/clientMessage.equals("koupit"))
            //     sendData("Zlaty dul".getBytes(), packet.getAddress(), packet.getPort());
        }
    }
}