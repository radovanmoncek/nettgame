package server.game.docker.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDU00Join;
import server.game.docker.net.MyPDU01Disconnect;
import server.game.docker.net.MyPDU02WorldInfo;
import server.game.docker.net.MyPDU03PlayerMove;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.example.game.logic.SimpleRTSGameServerSideLogic.TileType;
import server.game.docker.server.matchmaking.session.models.GameSessionClient;

class SimpleRTSGameClient {
    //Config
    /**
     * The server port
     */
    // public static final Integer serverPort = 4321;
    /**
     * The client IP address
     */
    // public static final String iP = "127.0.0.1";
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
    // private static enum TileType {
    //     NEXUS(Byte.MAX_VALUE), RIVER((byte) -1), RESOURCENODE((byte) 1), GOLDMINE((byte) 2), BRIDGE((byte) (Byte.MAX_VALUE - 1)), BLANK((byte) 0);

    //     private Byte tileID;

    //     private TileType(Byte tileID){
    //         this.tileID = tileID;
    //     }

    //     public Byte getTileID() {
    //         return tileID;
    //     }
    // }
    // private Integer winnerPlayerID;
    // private String playerName;
    // private String oponentName;
    //Networking
    private final Thread clientNetworkListener;
    // private final DatagramSocket socket;
    // private final InetAddress iPAddress;
    //Game logic
    //todo: only client copy - server must handle
    // private Integer playerGoldBalance = 0;
    // private Short playerMineBalance = 0 + 2;
    // private Long gameStartMills;
    // private Long lastGoldWithdraw;
    private Long clientID;
    private MyPDUActionHandler actionRouter;
    private String player1Name;
    private String player2Name;

    public SimpleRTSGameClient(/*DatagramSocket socket, InetAddress iPAddress*/String [] args){
        // this.socket = socket;
        // this.iPAddress = iPAddress;
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
            this.iPAddress = InetAddress.getByName(/*iPAddress*/"localhost");
        } catch (UnknownHostException | SocketException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        actionRouter = new MyPDUActionHandler()
            .withActionEntry(MyPDUTypes.INVALID.getPacketID(), p -> System.out.println("Invalid pakcet received"))
            .withActionEntry(MyPDUTypes.JOIN.getPacketID(), p -> {
                //A second player has joined, game must be starting by now
                sendUnicast(new MyPDU((byte) 04));
                // System.out.println("Session found");
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
                System.out.println(String.format("%s! %s has won", Long.parseLong(p.decode().get(0)) == clientID? "Victory" : "Defeat", p.decode().get(1)));
                System.exit(0);
            })
            .withActionEntry((byte) 07, p -> System.out.println(String.format("Gold update: %s", p.decode().get(0))));
        this.clientNetworkListener = new Thread(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // super.run();
            //     while(Objects.isNull(winnerPlayerID)){
                    
            //     }
            // }
        
                while(true/*Objects.isNull(winnerPlayerID)*/){
                    // sendData("koupit".getBytes());
                    byte [] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                    // System.out.println(String.format("Server sent: %s", new String(packet.getData()).trim()));
                    // parsePacket(data, packet.getAddress(), packet.getPort());
                    actionRouter.handle(packet);
                }
            }
        };
        clientNetworkListener.start();
        // gameMap = new byte[][] {
        //     {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        //     {0, 0, 0, 0, 0, Byte.MAX_VALUE, 0, 0, 0, 0, 0}
        // };

        //Generate resource nodes
        // System.out.println("Generating map ...");
        // for (int i = 1; (i < gameMap.length - 1)/* && (i != 5)*/; i++) {
        //     if(i == 5) 
        //         continue;
        //     for (int j = 0; j < gameMap[i].length; j++) 
        //         //1 in 5 (0 - 4) chance for a resource node
        //         gameMap[i][j] = new Random().nextInt(5) == 2? TileType.RESOURCENODE.tileID : TileType.BLANK.tileID;
        // }
        // System.out.println("Map generated");
        // System.out.println();

        //Draw map
        // drawMap();

        //todo: networking - await other player etc.

        //Start game
        System.out.println("The client has started");
        initInputLoop();
        // System.out.println("The client has started");
    }

    private void drawMap(){
        //Draw map
        for (int i = 0; i < gameMap.length; i++) {
            for (int j = 0; j < gameMap[i].length; j++) {
                if(gameMap[i][j] == TileType.NEXUS.getTileID())
                    System.out.print(String.format(/*"  %sN(%2s)%s  "*/"  %sN1%s  ", ANSI_GREEN,/* i != 0? player1Name : oponentplayer2Name,*/ ANSI_RESET));
                else if(gameMap[i][j] == TileType.NEXUS2.getTileID())
                    System.out.print(String.format(/*"  %sN(%2s)%s  "*/"  %sN2%s  ", ANSI_GREEN,/* i != 0? player1Name : oponentplayer2Name,*/ ANSI_RESET));
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
        // System.out.println(playerName);
        // System.out.print(String.format("gold: %d, mines: %d", playerGoldBalance, playerMineBalance));
    }

    private void initInputLoop(){
        //todo: implement game timer
        // gameStartMills = lastGoldWithdraw = Instant.now().toEpochMilli();
        
        //Run socket listener on separate thread

        // clientNetworkListener.start();
        //Login to game session - todo: maybe JWT auth??
        // System.out.print("Enter username, please: ");
        // sendUnicast(new Packet00Join(/*playerName = awaitPlayerInput()*/)/*.getData()*/);
        // drawMap();
        // System.out.println();
        System.out.println("Type 'join' to join a server");
        while(/*Objects.isNull(winnerPlayerID)*/true/* && false*/){
            // playerGoldBalance += withdrawGold();
            switch(awaitPlayerInput()){
                case "join" -> {
                    System.out.println("Joining a server ...");
                    sendUnicast(new MyPDU00Join());
                }
                case "exit" -> {
                    // winnerPlayerID = 1;
                    // Packet01Disconnect packet = new Packet01Disconnect(/*playerName*/clientID);
                    // sendUnicast(packet/*.getData()*/);
                    //todo: await game end acknowledgement
                    // System.exit(0);
                    sendUnicast(new MyPDU01Disconnect(clientID));
                }
                case "mine" -> {
                    // todo: e.g. mine E 4
                    // if(/*((playerGoldBalance += withdrawGold()) - 1000) >= 0*/false){
                        // playerMineBalance++;
                        // playerGoldBalance -= 1000;
                        // System.out.println(String.format("Bought a mine, new gold: %d, new mines: %d", playerGoldBalance, playerMineBalance));
                    // }
                    // else 
                        // System.out.println("Not enough for mine");
                    buyGoldMine();
                }
                case "bridge" -> {
                    // playerGoldBalance += withdrawGold();
                    // if(playerMineBalance > 4 && playerGoldBalance >= 400)
                    //     winnerPlayerID = 1;
                    // else
                    //     System.out.println("Not enough for bridge");
                    sendUnicast(new MyPDU03PlayerMove(clientID.toString(), "1"));
                }
                // case "gold" -> {
                //     System.out.println(String.format("You have %d gold", playerGoldBalance/* += withdrawGold()*/));
                //     // drawMap();
                // }
                default -> 
                    System.out.println("Unrecognised command");
            }
        }
        // System.out.println("You won");
    }

    private String awaitPlayerInput(){
        try /*(BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in)))*/ {
            BufferedReader playerInput = new BufferedReader(new InputStreamReader(System.in));
            return playerInput.readLine();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

    // private Integer withdrawGold(){
    //     //10 gold every 1 second
    //     final Integer withdrawnSum = playerMineBalance * (int) ((Instant.now().toEpochMilli() - lastGoldWithdraw) / 100);
    //     lastGoldWithdraw = Instant.now().toEpochMilli();
    //     return withdrawnSum;
    // }

    public void sendUnicast(/*byte[] data*/MyPDU p){
        DatagramPacket packet = new DatagramPacket(/*data*/p.getByteBuffer(), /*data.length*/p.getByteBuffer().length, iPAddress, 4321);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    // public void buyGoldMine(){
    //     // while(true){
    //         sendData("koupit".getBytes());
    //         byte [] data = new byte[1024];
    //         DatagramPacket packet = new DatagramPacket(data, data.length);
    //         try {
    //             socket.receive(packet);
    //         } catch (IOException e) {
    //             // TODO: handle exception
    //             e.printStackTrace();
    //         }
    //         System.out.println(String.format("Server sent: %s", new String(packet.getData()).trim()));
    //     // }
    // }

    @Deprecated
    private void parsePacket(byte[] data, InetAddress address, int port) { //todo: will become packet router Map<PacketType, PacketN> - Packet -> PacketRouter.route -> NO PacketN (e.g. game system Controller class) NO ClientIOCAction
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'parsePacket'");
        // PacketTypes type = Packet.lookupPacket(new String(data).trim().substring(0, 2));
        // switch (type) {
        //     case INVALID -> 
        //         System.out.println("Invalid pakcet received");

        //     case JOIN -> {
        //         Packet00Join joinPacket = new Packet00Join(data);
        //         // System.out.println(String.format("%s has joined", joinPacket.getUsername()));
        //         oponentName = joinPacket.getUsername();
        //         System.out.println(String.format("%s %s has joined%s", ANSI_GREEN, joinPacket.getUsername(), ANSI_RESET));
        //         drawMap();
        //         // System.out.print(String.format("%s has joined", joinPacket.getUsername()));
        //         // System.out.println();
        //         // addConnection(new GameClientPlayer(address, port, joinPacket.getUsername()), joinPacket);
        //     }

        //     case DISCONNECT -> {
        //         Packet01Disconnect disconnectPacket = new Packet01Disconnect(data);
        //         // System.out.println(String.format("%s %s has left the world %s", ANSI_RED, disconnectPacket.getUsername(), ANSI_RESET));
        //         oponentName = null;
        //         //Stop listening for updates from server
        //         // clientNetworkListener.
        //         // winnerPlayerID = 1;
        //         // System.exit(0);
        //         System.out.println(String.format("%s %s has left the world%s", ANSI_RED, disconnectPacket.getClientID(), ANSI_RESET));
        //         drawMap();
        //         // System.out.println();
        //     }

        //     case WORLDINFO -> {
        //         Packet02WorldInfo worldInfoPacket = new Packet02WorldInfo(data);
        //         if(worldInfoPacket.generated == 1)
        //             drawMap();
        //         else
        //             gameMap[worldInfoPacket.getI()][worldInfoPacket.getJ()] = TileType.RESOURCENODE.tileID;
        //     }
        
        //     default -> 
        //         System.out.println("Invalid packet received");
        // }
    }

    // private void addConnection(GameClientPlayer gameClientPlayer, Packet00Join joinPacket) {
    //     // TODO Auto-generated method stub
    //     // throw new UnsupportedOperationException("Unimplemented method 'addConnection'");
    // }
    // @FunctionalInterface
    // private interface ClientIOCAction {
    //     void perform();
    // }

    public void buyGoldMine(){
        // while(true){
            sendUnicast(new MyPDU03PlayerMove(/*"koupit"*/clientID.toString(), "0")/*.getBytes()*/);
            // byte [] data = new byte[1024];
            // DatagramPacket packet = new DatagramPacket(data, data.length);
            // try {
            //     socket.receive(packet);
            // } catch (IOException e) {
            //     // TODO: handle exception
            //     e.printStackTrace();
            // }
            // System.out.println(String.format("Server sent: %s", new String(packet.getData()).trim()));
        // }
    }

    // public void sendData(byte[] data){
    //     DatagramPacket packet = new DatagramPacket(data, data.length, iPAddress, 4321);
    //     try {
    //         socket.send(packet);
    //     } catch (IOException e) {
    //         // TODO: handle exception
    //         e.printStackTrace();
    //     }
    // }

    // public Integer getPort(){
    //     return socket.getPort();
    // }
}
