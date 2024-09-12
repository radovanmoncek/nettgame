package server.game.docker.examples.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDU00Join;
import server.game.docker.net.MyPDU01Disconnect;
import server.game.docker.net.MyPDU03PlayerMove;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.examples.game.logic.SimpleRTSGameServerSideLogic.TileType;

class SimpleRTSGameClient {
    //Config
    /**
     * The server port
     */
    public final Integer serverPort;
    /**
     * The server IP address
     */
    private InetAddress iPAddress;
    /**
     * The client socket
     */
    private DatagramSocket socket;

    public InetAddress getiPAddress() {
        return iPAddress;
    }
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final byte [][] gameMap;
    private final Thread clientNetworkListener;
    private Long clientID;
    private MyPDUActionHandler actionRouter;
    private String player1Name;
    private String player2Name;

    public SimpleRTSGameClient(String [] args){
        serverPort = 4321;
        gameMap = new byte[][] {
            {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0}
        };
        try {
            socket = new DatagramSocket();
            iPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        actionRouter = new MyPDUActionHandler()
            .withActionEntry(MyPDUTypes.INVALID.getPacketID(), p -> System.out.println("Invalid pakcet received"))
            .withActionEntry(MyPDUTypes.JOIN.getPacketID(), p -> {
                //A second player has joined, game must be starting by now
                sendUnicast(new MyPDU((byte) 04));
                //todo: speacial MyPDU about successfull connection to a game session
                System.out.println("Game started");
            })
            .withActionEntry(MyPDUTypes.DISCONNECT.getPacketID(), p -> {
                System.out.println("A player has forfeit, game is over");
                System.exit(0);
            })
            .withActionEntry(MyPDUTypes.WORLDINFO.getPacketID(), p -> {
                Vector<String> packetData = p.decode();
                Byte 
                    i = Byte.valueOf(packetData.get(0)),
                    j = Byte.valueOf(packetData.get(1)),
                    tileID = Byte.valueOf(packetData.get(2));
                gameMap[i][j] = tileID;
                drawMap();
            })
            .withActionEntry((byte) 05, p -> clientID = Long.valueOf(p.decode().get(0)))
            .withActionEntry((byte) 06, p -> {
                System.out.println(String.format("%s!", Long.parseLong(p.decode().get(0)) == clientID? "Victory" : "Defeat"));
                System.exit(0);
            })
            .withActionEntry((byte) 07, p -> System.out.println(String.format("Gold update: %s", p.decode().get(0))));
        this.clientNetworkListener = new Thread(){
            @Override
            public void run() {
                while(true){
                    byte [] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    actionRouter.handle(packet);
                }
            }
        };
        clientNetworkListener.start();
        //Give control to player
        initInputLoop();
    }

    private void drawMap(){
        //Draw map
        for (int i = 0; i < gameMap.length; i++) {
            for (int j = 0; j < gameMap[i].length; j++) {
                if(gameMap[i][j] == TileType.NEXUS.getTileID())
                    System.out.print(String.format("  %sN1%s  ", ANSI_GREEN, ANSI_RESET));
                else if(gameMap[i][j] == TileType.NEXUS2.getTileID())
                    System.out.print(String.format("  %sN2%s  ", ANSI_GREEN, ANSI_RESET));
                else if(gameMap[i][j] == TileType.RESOURCENODE.getTileID())
                    System.out.print(String.format("  %sG(a)%s  ", ANSI_YELLOW, ANSI_RESET));
                else if(gameMap[i][j] == TileType.GOLDMINE.getTileID())
                    System.out.print(String.format("  %sM()%s  ", ANSI_PURPLE, ANSI_RESET));
                else if(gameMap[i][j] == TileType.RIVER.getTileID())
                    System.out.print(String.format("  %s====%s  ", ANSI_BLUE, ANSI_RESET));
                else
                    System.out.print(String.format("  %4d  ", gameMap[i][j]));
            }
            System.out.println();
        }
    }

    private void initInputLoop(){
        System.out.println("Type 'join' to join a session");
        while(true){
            switch(awaitPlayerInput()){
                case "join" -> {
                    System.out.println("Joining a server ...");
                    sendUnicast(new MyPDU00Join());
                }
                case "exit" -> 
                    sendUnicast(new MyPDU01Disconnect(clientID));
                case "mine" -> 
                    buyGoldMine();
                case "bridge" -> 
                    sendUnicast(new MyPDU03PlayerMove(clientID.toString(), "1"));
                default -> 
                    System.out.println("Unrecognised command");
            }
        }
    }

    private String awaitPlayerInput(){
        try /*(BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in)))*/ {
            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            return playerInput.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendUnicast(MyPDU p){
        DatagramPacket packet = new DatagramPacket(p.getByteBuffer(), p.getByteBuffer().length, iPAddress, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buyGoldMine(){
        sendUnicast(new MyPDU03PlayerMove(clientID.toString(), "0"));
    }
}
