package server.game.docker.server.matchmaking.session.examples.game.logic;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import server.game.docker.net.MyPDU;
import server.game.docker.net.MyPDU02WorldInfo;
import server.game.docker.net.MyPDUActionHandler;
import server.game.docker.net.MyPDUTypes;
import server.game.docker.server.matchmaking.session.GameSession;
import server.game.docker.server.matchmaking.session.examples.game.models.GameClientPlayer;
import server.game.docker.server.matchmaking.session.models.GameSessionClient;

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
    //Session metadata
    private Long gameStartMills;
    private Long gameSessionID;

    public SimpleRTSGameServerSideLogic(GameSession gameSession){
        this.gameSession = gameSession;
        players = new Vector<>();
        gameMap = new byte[][] {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID, TileType.RIVER.tileID},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
        availableGoldMinesPool1 = new LinkedList<>();
        availableGoldMinesPool2 = new LinkedList<>();
    }

    private void generateWorld() {
        System.out.println("Generating map ...");
        for (byte i = 1; (i < gameMap.length - 1); i++) {
            if(i == 5) 
                continue;
            for (byte j = 0; j < gameMap[i].length; j++) {
                //1 in 5 (0 - 4) chance for a resource node
                Boolean isRersourceNode = new Random().nextInt(5) == 2;
                gameMap[i][j] = isRersourceNode? TileType.RESOURCENODE.tileID : TileType.BLANK.tileID;
                if(isRersourceNode){
                    if(i <= 4) 
                        availableGoldMinesPool1.add(0, new byte[]{i, j});
                    else 
                        availableGoldMinesPool2.add(0, new byte[]{i, j});
                    gameSession.sendMulticast(new MyPDU02WorldInfo(i, j, TileType.RESOURCENODE.tileID));
                }
            }
            if(i == gameMap.length - 2){
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
                                            //Each mine has a production quota of 100 gold per 10 seconds (tick)
                                            p.setGold(p.getGold() + 100);
                                MyPDU serverGoldTickInfo = new MyPDU((byte) 07, p.getGold().toString());
                                GameSessionClient currentGameSessionClient = gameSession.connectedClients.stream().filter(c -> c.getClientID().equals(p.getClientID())).findAny().orElse(null);
                                serverGoldTickInfo.setAddress(currentGameSessionClient.getiPAddress());
                                serverGoldTickInfo.setPort(currentGameSessionClient.getPort());
                                gameSession.sendUnicast(serverGoldTickInfo);
                            });
                        }
                    }, 0, 5000);
                }
            })
            .withActionEntry(MyPDUTypes.PLAYERMOVE.getPacketID(), new MyPDUActionHandler.MyPDUAction() {
                public synchronized void perform(MyPDU p){
                    Vector<String> packetData = p.decode();
                    GameClientPlayer currentPlayer = players.stream().filter(player -> Long.valueOf(packetData.get(0)).equals(player.getClientID())).findAny().orElse(null);
                    switch(packetData.get(1)){
                        case "0" -> {
                            //todo: e.g. mine E 4 (Client / Player serlectable position)
                            if((currentPlayer.getGold() - 1000) >= 0 && currentPlayer.getMaximumOwnableArea()[1] <= 4? !availableGoldMinesPool1.isEmpty() : !availableGoldMinesPool2.isEmpty()){
                                currentPlayer.setGold(currentPlayer.getGold() - 1000);
                                byte [] boughtMine = currentPlayer.getMaximumOwnableArea()[1] <= 4? availableGoldMinesPool1.removeLast() : availableGoldMinesPool2.removeLast();
                                gameMap[boughtMine[0]][boughtMine[1]] = TileType.GOLDMINE.tileID;
                                gameSession.sendMulticast(new MyPDU02WorldInfo(boughtMine[0], boughtMine[1], TileType.GOLDMINE.tileID));
                                System.out.println(String.format("Player %s just bought a mine %d %d", currentPlayer.getUsername(), boughtMine[0], boughtMine[1]));
                            }
                        }
                        case "1" -> {
                            if(currentPlayer.getGold() >= 10000){
                                gameSession.sendMulticast(new MyPDU((byte) 06, currentPlayer.getClientID().toString(), currentPlayer.getUsername()));
                                gameSession.endSession(currentPlayer.getUsername());
                            }
                        }
                    }
                }
            });
    }
}
