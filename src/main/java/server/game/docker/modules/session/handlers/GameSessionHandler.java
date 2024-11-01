package server.game.docker.modules.session.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.game.docker.ship.parents.pdus.PDU;

import java.net.InetAddress;

public class GameSessionHandler extends SimpleChannelInboundHandler<PDU> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PDU pdu) throws Exception {

    }

    /**
     * Server side player client representation
     */
    public static class GameSessionClient {
        private final InetAddress clientIPAddress;
        private final Integer port;
        private final Long clientID;
        public InetAddress getClientIPAddress() {
            return clientIPAddress;
        }
        public Integer getPort() {
            return port;
        }
        public Long getClientID() {
            return clientID;
        }
        public GameSessionClient(InetAddress clientIPAddress, Integer port, Long clientID) {
            this.clientIPAddress = clientIPAddress;
            this.port = port;
            this.clientID = clientID;
        }

        public static class GameClientPlayer {
            private final Long clientID;
            private String username;
            private Boolean winner;
            private Integer gold;
            private Byte [] maximumOwnableArea;

            public Boolean getWinner() {
                return winner;
            }

            public Byte [] getMaximumOwnableArea() {
                return maximumOwnableArea;
            }

            public void setMaximumOwnableArea(Byte [] maximumOwnableArea) {
                this.maximumOwnableArea = maximumOwnableArea;
            }

            public void setWinner(Boolean winner) {
                this.winner = winner;
            }

            public Integer getGold() {
                return gold;
            }

            public void setGold(Integer gold) {
                this.gold = gold;
            }

            public GameClientPlayer(final GameSessionClient gameClient){
                clientID = gameClient.getClientID();
                gold = 10000;
                setUsername("Player " + gameClient.getClientID().toString());
            }
            public Long getClientID() {
                return clientID;
            }
            public String getUsername() {
                return username;
            }
            public void setUsername(String username) {
                this.username = username;
            }
        }
    }

    public static class SimpleRTSGameServerSideLogic {
    //    private final GameSession gameSession;
    //    private final Vector<GameClientPlayer> players;
    //    private final byte [][] gameMap;
    //    private final LinkedList<byte[]> availableGoldMinesPool1, availableGoldMinesPool2;
        //Session metadata
        private Long gameStartMills;
        private Long gameSessionID;

        public SimpleRTSGameServerSideLogic(GameSessionHandler gameSession){
    //        this.gameSession = gameSession;
    //        registerActionMappings();
    //        players = new Vector<>();
    //        gameMap = new byte[][] {
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID(), TileType.RIVER.getTileID()},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    //        };
    //        availableGoldMinesPool1 = new LinkedList<>();
    //        availableGoldMinesPool2 = new LinkedList<>();
        }

        private void generateWorld() {
    //        players.get(0).setMaximumOwnableArea(new Byte[]{1, 4});
    //        players.get(1).setMaximumOwnableArea(new Byte[]{6, 9});
    //        System.out.println("Generating map ...");
    //        for (byte i = 1; (i < gameMap.length - 1); i++) {
    //            GameSessionClient currentClientPlayer = i <= 4? gameSession.connectedClients.get(0) : gameSession.connectedClients.get(1);
    //            if(i == 5)
    //                continue;
    //            for (byte j = 0; j < gameMap[i].length; j++) {
                    //1 in 5 (0 - 4) chance for a resource node
    //                final boolean isResourceNode = new Random().nextInt(5) == 2;
    //                gameMap[i][j] = isResourceNode? TileType.RESOURCENODE.getTileID() : TileType.BLANK.getTileID();
    //                if(isResourceNode){
    //                    if(i <= 4)
    //                        availableGoldMinesPool1.add(0, new byte[]{i, j});
    //                    else
    //                        availableGoldMinesPool2.add(0, new byte[]{i, j});
    //                    gameSession.sendUnicast(PDUBody.fromWorldInfoData(i, j, TileType.RESOURCENODE.getTileID()).withIPAndPort(currentClientPlayer.getClientIPAddress(), currentClientPlayer.getPort()));
    //                }
    //            }
    //            if(i == gameMap.length - 2){
                    //Player side decision and fog of war
    //                gameSession.sendUnicast(PDUBody.fromWorldInfoData((byte) 0, (byte) 5, Byte.MAX_VALUE).withIPAndPort(findSessionClientWithPlayerClientID(players.get(0).getClientID()).getClientIPAddress(), findSessionClientWithPlayerClientID(players.get(0).getClientID()).getPort()));
    //                gameSession.sendUnicast(PDUBody.fromWorldInfoData((byte) 10, (byte) 5, Byte.MIN_VALUE).withIPAndPort(findSessionClientWithPlayerClientID(players.get(1).getClientID()).getClientIPAddress(), findSessionClientWithPlayerClientID(players.get(1).getClientID()).getPort()));
    //            }
    //        }
    //        System.out.println("Map generated");
        }

    //    private void registerActionMappings(){
    //        gameSession.getActionMapper()
                //Game start
    //            .withMapping(PDUType.GAMESTART.getID(), p -> {
                    //Ensure that this may only happen once per session
    //                if(gameSession.connectedClients.size() == 2 && Objects.isNull(gameStartMills)){
    //                    gameStartMills = Instant.now().toEpochMilli();
    //                    players.addAll(gameSession.connectedClients.stream().map(GameClientPlayer::new).toList());
    //                    generateWorld();
                        //Server "tick functionality" (initiate)
    //                    new Timer().scheduleAtFixedRate(new TimerTask() {
    //                        public void run(){
    //                            players.forEach(p -> {
    //                                short playerMineCount = 0;
    //                                for (byte i = p.getMaximumOwnableArea()[0]; i <= p.getMaximumOwnableArea()[1]; i++)
    //                                    for (byte j = 0; j < gameMap[i].length; j++)
    //                                        if(gameMap[i][j] == TileType.GOLDMINE.getTileID()) {
                                                //Each mine has a production quota of 100 gold per 10 seconds (tick)
    //                                            p.setGold(p.getGold() + 10);
    //                                            makePlayerGoldTransaction(p, 10);
    //                                            playerMineCount++;
    //                                        }
    //                                PDUBody serverGoldTickInfo = new PDUBody(PDUType.SERVERTICKUPDATE.getID(), p.getGold().toString(), Short.toString((short) (playerMineCount * 10)));
    //                                GameSessionClient currentGameSessionClient = gameSession.connectedClients.stream().filter(c -> c.getClientID().equals(p.getClientID())).findAny().orElse(null);
    //                                if(currentGameSessionClient == null)
    //                                    return;
    //                                serverGoldTickInfo.setAddress(currentGameSessionClient.getClientIPAddress());
    //                                serverGoldTickInfo.setPort(currentGameSessionClient.getPort());
    //                                gameSession.sendUnicast(serverGoldTickInfo);
    //                            });
    //                        }
    //                    }, 0, 1000);
    //                    gameSession.sendMulticast(new PDUBody(PDUType.GAMESTART.getID()));
    //                }
    //            })
    //            .withMapping(PDUType.PLAYERMOVE.getID(), p -> {
    //                    Vector<String> packetData = p.decode();
    //                    GameClientPlayer currentPlayer;
    //                    if((currentPlayer = players.stream().filter(player -> Long.valueOf(packetData.get(0)).equals(player.getClientID())).findAny().orElse(null)) == null)
    //                        return;
    //                    byte i = Byte.parseByte(packetData.get(1)),
    //                            j = Byte.parseByte(packetData.get(2));
    //                    switch(packetData.get(3)){
    //                        case "0" -> {
                                //todo: e.g. mine E 4 (Client / Player selectable position)
    //                            System.out.printf("Player %s with %d can buy goldmine: enough gold: %b | gold after transaction: %d\n", currentPlayer.getUsername(), currentPlayer.getGold(), (currentPlayer.getGold() - 1000) >= 0, currentPlayer.getGold() - 1000);
    //                            if(/*((currentPlayer.getGold() - 1000) >= 0) && (currentPlayer.getMaximumOwnableArea()[1] <= 4? !availableGoldMinesPool1.isEmpty() : !availableGoldMinesPool2.isEmpty())*/
    //                            gameMap[i][j] == TileType.RESOURCENODE.getTileID() && (currentPlayer.getMaximumOwnableArea()[0] <= i && i <= currentPlayer.getMaximumOwnableArea()[1])){
    //                                currentPlayer.setGold(currentPlayer.getGold() - 1000);
    //                                if(!makePlayerGoldTransaction(currentPlayer, -1000))
    //                                    return;
    //                                byte [] boughtMine = new byte[]{i, j}/*currentPlayer.getMaximumOwnableArea()[1] <= 4? availableGoldMinesPool1.removeLast() : availableGoldMinesPool2.removeLast()*/;
    //                                gameMap[boughtMine[0]][boughtMine[1]] = TileType.GOLDMINE.getTileID();
    //                                gameSession.sendMulticast(PDUBody.fromWorldInfoData(boughtMine[0], boughtMine[1], TileType.GOLDMINE.getTileID()));
    //                                System.out.printf("Player %s just bought a mine %d %d | new balance: %d\n", currentPlayer.getUsername(), boughtMine[0], boughtMine[1], currentPlayer.getGold());
    //                            }
    //                        }
    //                        case "1" -> {
    //                            System.out.printf("Player %s wants to buy bridge | can: %b gold: %d\n", currentPlayer.getUsername(), accessPlayerGoldFunds(currentPlayer) >= 10000, accessPlayerGoldFunds(currentPlayer));
    //                            if(accessPlayerGoldFunds(currentPlayer) >= 10000){
    //                                gameSession.sendMulticast(new PDUBody(PDUType.GAMEEND.getID(), currentPlayer.getClientID().toString(), currentPlayer.getUsername()));
    //                                gameSession.endSession(currentPlayer.getUsername());
    //                            }
    //                        }
    //                    }
    //            });
    //    }

    //    private synchronized Integer accessPlayerGoldFunds(GameClientPlayer currentPlayer) {
    //        return currentPlayer.getGold();
    //    }
    //
    //    private synchronized boolean makePlayerGoldTransaction(GameClientPlayer currentPlayer, Integer goldAmount) {
    //        if((goldAmount < 0) && (currentPlayer.getGold() + goldAmount < 0))
    //            return false;
    //        currentPlayer.setGold(currentPlayer.getGold() + goldAmount);
    //        return true;
    //    }
    //
    //    private GameSessionClient findSessionClientWithPlayerClientID(Long clientID){
    //        return gameSession.connectedClients.stream().filter(c -> c.getClientID().equals(clientID)).findAny().orElse(null);
    //    }
    }

    public enum TileType {
        NEXUS(Byte.MAX_VALUE), NEXUS2(Byte.MIN_VALUE), RIVER((byte) -1), RESOURCENODE((byte) 1), GOLDMINE((byte) 2), BRIDGE((byte) (Byte.MAX_VALUE - 1)), BLANK((byte) 0);

        private final Byte tileID;

        TileType(Byte tileID) {
            this.tileID = tileID;
        }

        public Byte getTileID() {
            return tileID;
        }
    }
}