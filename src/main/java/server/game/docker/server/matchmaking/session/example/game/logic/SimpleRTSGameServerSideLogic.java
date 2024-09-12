package server.game.docker.server.matchmaking.session.example.game.logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.server.matchmaking.session.GameSession;
import server.game.docker.server.matchmaking.session.example.game.models.GameClientPlayer;
import server.game.docker.server.matchmaking.session.models.GameSessionClient;
import server.game.docker.net.MyPDU00Join;
import server.game.docker.net.MyPDU01Disconnect;
import server.game.docker.net.MyPDU02WorldInfo;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;

public class SimpleRTSGameServerSideLogic {
    private final GameSession gameSession;
    private final Vector<GameClientPlayer> players;
    private final byte [][] gameMap;
    public static enum TileType {
        NEXUS(Byte.MAX_VALUE), NEXUS2(Byte.MIN_VALUE), RIVER((byte) -1), RESOURCENODE((byte) 1), GOLDMINE((byte) 2), BRIDGE((byte) (Byte.MAX_VALUE - 1)), BLANK((byte) 0);

        private Byte tileID;

        private TileType(Byte tileID){
            this.tileID = tileID;
        }

        public Byte getTileID() {
            return tileID;
        }
    }
    private final LinkedList<byte[]> availableGoldMinesPool1, availableGoldMinesPool2;
    // private Integer winnerPlayerID;
    //Game logic
    //todo: only client copy - server must handle
    // private Integer playerGoldBalance = 0;
    // private Short playerMineBalance = 0 + 2;
    //Session metadata
    private Long gameStartMills;
    private Long gameSessionID;
    // private Long lastGoldWithdraw;

    public SimpleRTSGameServerSideLogic(GameSession gameSession){
        this.gameSession = gameSession;
        players = new Vector<>(/*gameSession.connectedClients.stream().map(GameClientPlayer::new).toList()*/);
        gameMap = new byte[][] {
            {0, 0, 0, 0, 0, /*Byte.MAX_VALUE*/0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, /*Byte.MAX_VALUE*/0, 0, 0, 0, 0, 0}
        };
        availableGoldMinesPool1 = new LinkedList<>();
        availableGoldMinesPool2 = new LinkedList<>();

        //Generate resource nodes
        // System.out.println("Generating map ...");
        // for (int i = 1; (i < gameMap.length - 1)/* && (i != 5)*/; i++) {
        //     if(i == 5) 
        //         continue;
        //     for (int j = 0; j < gameMap[i].length; j++) {
        //         //1 in 5 (0 - 4) chance for a resource node
        //         Boolean isRersourceNode = new Random().nextInt(5) == 2;
        //         gameMap[i][j] = isRersourceNode? TileType.RESOURCENODE.tileID : TileType.BLANK.tileID;
        //         if(isRersourceNode)
        //             sendToAll(new Packet02WorldInfo(new byte[] {(byte) i, (byte) j}).getData());
        //     }
        // }
        // System.out.println("Map generated");
        // System.out.println();

        // initGameLoop();
    }

    // private Integer withdrawGold(){
    //     //10 gold every 1 second
    //     final Integer withdrawnSum = playerMineBalance * (int) ((Instant.now().toEpochMilli() - lastGoldWithdraw) / 100);
    //     lastGoldWithdraw = Instant.now().toEpochMilli();
    //     return withdrawnSum;
    // }

    // @Deprecated
    // private void initGameLoop(){
        //todo: implement game timer
        // gameStartMills = lastGoldWithdraw = Instant.now().toEpochMilli();
        /*while(Objects.isNull(winnerPlayerID)){
            playerGoldBalance += withdrawGold();
            switch(*//*awaitPlayerInput()*//*"placeholder"){
                case "exit" -> winnerPlayerID = 1;
                case "mine" -> {
                    //todo: e.g. mine E 4
                    if(((playerGoldBalance += withdrawGold()) - 1000) >= 0){
                        playerMineBalance++;
                        playerGoldBalance -= 1000;
                        System.out.println(String.format("Bought a mine, new gold: %d, new mines: %d", playerGoldBalance, playerMineBalance));
                    }
                    else 
                        System.out.println("Not enough for mine");
                }
                case "bridge" -> {
                    playerGoldBalance += withdrawGold();
                    if(playerMineBalance > 4 && playerGoldBalance >= 400)
                        winnerPlayerID = 1;
                    else
                        System.out.println("Not enough for bridge");
                }
                case "gold" -> {
                    System.out.println(String.format("You have %d gold", playerGoldBalance += withdrawGold()));
                }
                default -> 
                    System.out.println("Unrecognised command");
            }
        }*/
        // System.out.println("You won");
    // }

    @Deprecated
    public void parsePacket(byte[] data, InetAddress address, int port) { //todo: will become packet router Map<PacketType, PacketN> - Packet -> PacketRouter.route -> NO PacketN (e.g. game system Controller class)
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'parsePacket'");
        // PacketTypes type = Packet.lookupPacket(new String(data).trim().substring(0, 2));
        // switch (type) {
        //     case INVALID -> 
        //         System.out.println("Invalid pakcet received");

        //     case JOIN -> {
        //         Packet00Join joinPacket = new Packet00Join(data);
        //         System.out.println(String.format("A player (%s / %s) has joined the session", joinPacket.getUsername(), address.getHostAddress()));
        //         addConnection(new GameClientPlayer(address, port, joinPacket.getUsername()), joinPacket);
        //         if(gameSession.connectedClients.size() == 2)
        //             generateWorld();
        //     }

        //     case DISCONNECT -> {
        //         Packet01Disconnect disconnectPacket = new Packet01Disconnect(data);
        //         System.out.println(String.format("A player (%s / %s) has left the session", disconnectPacket.getUsername(), address.getHostAddress()));
        //         gameSession.connectedClients.remove(gameSession.connectedClients.indexOf(gameSession.connectedClients.stream().filter(c -> disconnectPacket.getUsername().equals(c.getUsername())).findAny().orElse(null)));
        //         sendToAll(data);
        //     }

            // case WORLDINFO -> {
            //     Packet02WorldInfo worldInfoPacket = new Packet02WorldInfo(data);
            //     gameMap[worldInfoPacket.getI()][worldInfoPacket.getJ()] = TileType.RESOURCENODE.tileID;
            // }
        
            // default -> 
            //     System.out.println("Invalid packet received");
        // }
    }

    private void generateWorld() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'generateWorld'");
        System.out.println("Generating map ...");
        for (byte i = 1; (i < gameMap.length - 1)/* && (i != 5)*/; i++) {
            if(i == 5) 
                continue;
            for (byte j = 0; j < gameMap[i].length; j++) {
                //1 in 5 (0 - 4) chance for a resource node
                Boolean isRersourceNode = new Random().nextInt(5) == 2;
                gameMap[i][j] = isRersourceNode? TileType.RESOURCENODE.tileID : TileType.BLANK.tileID;
                if(isRersourceNode){
                    // gameSession.sendMulticast(new Packet02WorldInfo("02".concat(Byte.toString((byte) i)).concat(",").concat(Byte.toString((byte) j)).concat(",").concat(Byte.toString((byte) (/*i == gameMap.length - 1 && j == gameMap[i].length -1? 1 : */0))).getBytes()).getData());
                    if(i <= 4) 
                        availableGoldMinesPool1.add(0, new byte[]{i, j});
                    else 
                        availableGoldMinesPool2.add(0, new byte[]{i, j});
                    gameSession.sendMulticast(new MyPDU02WorldInfo(i, j, /*(byte) 0*/TileType.RESOURCENODE.tileID));
                }
            }
            if(i == gameMap.length - 2/* && j == gameMap[i].length -1*/){
                // gameSession.sendMulticast(new Packet02WorldInfo("02".concat(Byte.toString((byte) -1)).concat(",").concat(Byte.toString((byte) -1)).concat(",").concat(Byte.toString((byte) 1)).getBytes()).getData());
                // gameSession.sendMulticast(new Packet02WorldInfo((byte) -1, (byte) -1, (byte) 1));
                //Player side decision
                players.get(0).setMaximumOwnableArea(new Byte[]{1, 4});
                gameSession.sendMulticast(new MyPDU02WorldInfo((byte) 0, (byte) 5, /*players.get(0).getUsername()*/Byte.MAX_VALUE));
                players.get(1).setMaximumOwnableArea(new Byte[]{6, 9});
                gameSession.sendMulticast(new MyPDU02WorldInfo((byte) 10, (byte) 5, /*players.get(players.size() - 1).getUsername()*/Byte.MIN_VALUE));
            }
        }
        System.out.println("Map generated");
    }

    public MyPDUActionHandler injectPacketActionHandler(MyPDUActionHandler actionRouter){
        return actionRouter
            //Game start
            .withActionEntry((byte) 04, p -> {
                //Ensure that this may only happen once per session
                if(gameSession.connectedClients.size() == 2 && Objects.isNull(gameStartMills)){
                    gameStartMills = Instant.now().toEpochMilli();
                    players.addAll(gameSession.connectedClients.stream().map(GameClientPlayer::new).toList());
                    generateWorld();
                    //Server "tick functionality" (initiate)
                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        public synchronized void run(){
                            players.forEach(p -> {
                                for (byte i = p.getMaximumOwnableArea()[0]; i <= p.getMaximumOwnableArea()[1]; i++) 
                                    for (byte j = 0; j < gameMap[i].length; j++) 
                                        if(gameMap[i][j] == TileType.GOLDMINE.tileID)
                                            //Each mine has a production quota of 100 gold per second (tick)
                                            p.setGold(p.getGold() + 100);
                                MyPDU serverGoldTickInfo = new MyPDU((byte) 07, p.getGold().toString());
                                GameSessionClient currentGameSessionClient = gameSession.connectedClients.stream().filter(c -> c.getClientID().equals(p.getClientID())).findAny().orElse(null);
                                serverGoldTickInfo.setAddress(currentGameSessionClient.getiPAddress());
                                serverGoldTickInfo.setPort(currentGameSessionClient.getPort());
                                gameSession.sendUnicast(serverGoldTickInfo);
                            });
                        }
                    }, 0, 10000);
                }
            })
            .withActionEntry(MyPDUTypes.PLAYERMOVE.getPacketID(), p -> {
                Vector<String> packetData = p.decode();
                GameClientPlayer currentPlayer = players.stream().filter(player -> Long.valueOf(packetData.get(0)).equals(player.getClientID())).findAny().orElse(null);
                switch(packetData.get(1)){
                    case "0" -> {
                        //todo: e.g. mine E 4
                    if((/*(playerGoldBalance += withdrawGold())*/currentPlayer.getGold() - 1000) >= 0 && currentPlayer.getMaximumOwnableArea()[1] <= 4? !availableGoldMinesPool1.isEmpty() : !availableGoldMinesPool2.isEmpty()){
                        // playerMineBalance++;
                        // playerGoldBalance -= 1000;
                        // System.out.println(String.format("Bought a mine, new gold: %d, new mines: %d", playerGoldBalance, playerMineBalance));
                        currentPlayer.setGold(currentPlayer.getGold() - 1000);
                        byte [] boughtMine = currentPlayer.getMaximumOwnableArea()[1] <= 4? availableGoldMinesPool1.removeLast() : availableGoldMinesPool2.removeLast();
                        gameMap[boughtMine[0]][boughtMine[1]] = TileType.GOLDMINE.tileID;
                        gameSession.sendMulticast(new MyPDU02WorldInfo(boughtMine[0], boughtMine[1], TileType.GOLDMINE.tileID));
                        System.out.println(String.format("Player %s just bought a mine %d %d", currentPlayer.getUsername(), boughtMine[0], boughtMine[1]));
                    }
                    // else 
                        // System.out.println("Not enough for mine");
                    }
                    case "1" -> {
                        // playerGoldBalance += withdrawGold();
                        // if(playerMineBalance > 4 && playerGoldBalance >= 400)
                            // winnerPlayerID = 1;//todo: special game winner packet
                        // else
                            // System.out.println("Not enough for bridge");
                        if(currentPlayer.getGold() >= 10000){
                            gameSession.sendMulticast(new MyPDU((byte) 06, currentPlayer.getClientID().toString(), currentPlayer.getUsername()));
                            gameSession.endSession(currentPlayer.getUsername());
                        }
                    }
                }
            });
    }
}
