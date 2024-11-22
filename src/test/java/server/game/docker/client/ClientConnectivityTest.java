package server.game.docker.client;

import io.netty.channel.ChannelId;
import org.junit.jupiter.api.*;
import server.game.docker.GameServer;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.messages.facades.ChatMessageClientFacade;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;
import server.game.docker.client.modules.sessions.facades.SessionClientFacade;
import server.game.docker.client.modules.state.facades.StateClientFacade;
import server.game.docker.client.modules.state.pdus.StateRequestPDU;
import server.game.docker.modules.chat.facades.ChatMessageServerFacade;
import server.game.docker.modules.session.facades.SessionServerFacade;
import server.game.docker.modules.state.facades.StateServerFacade;
import server.game.docker.ship.parents.pdus.PDU;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientConnectivityTest {
    private static GameClient gameClient, gameClient2;
    private static GameServer gameServer;
    private static String nickname1, nickname2;
    private static Long lobbyLeaderId1, lobbyLeaderId2;
    private static Collection<String> lobbyMembers1, lobbyMembers2;
    private static boolean sessionMember1, sessionMember2;
    private static Integer x1, y1, x2, y2;
    private static String message1, message2;
    private static String messageNickname1, messageNickname2;

    @BeforeAll
    static void setup() throws Exception {
        gameServer = GameServer
                .newInstance()
                .withSessionServerFacadeFactory(() -> new SessionServerFacade(){
                    private Long tickCounter = 0L;

                    @Override
                    public void receiveSessionTick(ChannelId playerId, Map<ChannelId, String> playerLobby, PDU protocolDataUnit, StateServerFacade stateServerFacade) {
                        System.out.printf("playerNickname: %s, playerLobby: %s, PDU: %s, ticks: %d\n", playerId, playerLobby, protocolDataUnit, ++tickCounter); //todo: log4j
                        if(protocolDataUnit instanceof StateRequestPDU stateRequestPDU) {
                            System.out.printf("Player:%s x:%d y:%d\n", playerId, stateRequestPDU.x(), stateRequestPDU.y());//todo: log4j
                            stateServerFacade.respondToStateRequest(playerLobby.get(playerId), stateRequestPDU.x(), stateRequestPDU.y(), playerLobby.keySet().toArray(ChannelId[]::new));
                        }
                    }
                })
                .withStateServerFacade(new StateServerFacade())
                .withChatMessageServerFacade(new ChatMessageServerFacade());
        new Thread(() -> {
            try {
                gameServer.run();
            } catch (InterruptedException e) {
                e.printStackTrace(); //todo: log4j
            }
        }).start();

        TimeUnit.SECONDS.sleep(4);

        gameClient = GameClient
                .newInstance()
                .withPlayerClientFacade(new PlayerClientFacade() {
                    @Override
                    public void receiveNewNickname(String newNickname) {
                        nickname1 = newNickname;
                    }
                })
                .withLobbyClientFacade(new LobbyClientFacade() {
                    @Override
                    public void receiveLobbyLeft(Long leaderId, Collection<String> members) {
                        lobbyLeaderId1 = leaderId;
                        lobbyMembers1 = members;
                    }

                    @Override
                    public void receiveLobbyJoined(Long leaderId, Collection<String> members) {
                        lobbyLeaderId1 = leaderId;
                        lobbyMembers1 = members;
                    }

                    @Override
                    public void receiveLobbyCreated(Long leaderId, Collection<String> members) {
                        lobbyLeaderId1 = leaderId;
                        lobbyMembers1 = members;
                    }
                })
                .withSessionClientFacade(new SessionClientFacade() {
                    @Override
                    public void receiveStartSessionResponse() {
                        sessionMember1 = true;
                    }

                    @Override
                    public void receiveStopSessionResponse() {
                        sessionMember1 = false;
                    }
                })
                .withStateClientFacade(new StateClientFacade() {
                    @Override
                    public void receiveState(String playerNickname, Integer x, Integer y) {
                        nickname1 = playerNickname;
                        x1 = x;
                        y1 = y;
                    }
                })
                .withChatMessageClientFacade(new ChatMessageClientFacade() {
                    @Override
                    public void receivePlayerLobbyChatMessage(String playerNickname, String message) {
                        messageNickname1 = playerNickname;
                        message1 = message;
                    }
                });

        gameClient.run(0);
    }

    @Test
    @Order(1)
    void connectionTest() {
        assertTrue(gameClient.isConnected());
    }

    @Test
    @Order(2)
    void usernameRequestAndReceiveTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> gameClient.getUsernameClientFacade().requestNickname("TestThatIsWayOverTheLimitOf8Characters"));
        gameClient.getUsernameClientFacade().requestNickname("Test");

        for (int i = 0; i < 10 && nickname1 == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals("Test", nickname1);
    }

    @Test
    @Order(3)
    void createLobbyTest() throws Exception {
        gameClient.getLobbyFacade().createLobby();
        for (int i = 0; i < 10 && (lobbyLeaderId1 == null || lobbyLeaderId1.equals(-1L)); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(1, lobbyMembers1.size());
        assertTrue(lobbyMembers1.contains(nickname1));
    }

    @Order(4)
    @RepeatedTest(4)
    void joinLobbyTest() throws Exception {
        resetGameClientSingletonInstance();
        if (Objects.isNull(gameClient2)) {
            gameClient2 = GameClient
                    .newInstance()
                    .withPlayerClientFacade(new PlayerClientFacade() {
                        @Override
                        public void receiveNewNickname(String newNickname) {
                            nickname2 = newNickname;
                        }
                    })
                    .withLobbyClientFacade(new LobbyClientFacade() {
                        @Override
                        public void receiveLobbyJoined(Long leaderChannelId, Collection<String> members) {
                            lobbyLeaderId2 = leaderChannelId;
                            lobbyMembers2 = members;
                        }

                        @Override
                        public void receiveLobbyLeft(Long leaderId, Collection<String> members) {
                            lobbyLeaderId2 = leaderId;
                            lobbyMembers2 = members;
                        }

                        @Override
                        public void receiveLobbyCreated(Long leaderId, Collection<String> members) {
                            lobbyLeaderId2 = leaderId;
                            lobbyMembers2 = members;
                        }
                    })
                    .withSessionClientFacade(new SessionClientFacade(){
                        @Override
                        public void receiveStartSessionResponse() {
                            sessionMember2 = true;
                        }

                        @Override
                        public void receiveStopSessionResponse() {
                            sessionMember2 = false;
                        }
                    })
                    .withStateClientFacade(new StateClientFacade() {
                        @Override
                        public void receiveState(String playerNickname, Integer x, Integer y) {
                            nickname2 = playerNickname;
                            x2 = x;
                            y2 = y;
                        }
                    })
                    .withChatMessageClientFacade(new ChatMessageClientFacade(){
                        @Override
                        public void receivePlayerLobbyChatMessage(String playerNickname, String message) {
                            messageNickname2 = playerNickname;
                            message2 = message;
                        }
                    });

            gameClient2.run(0);

            gameClient2.getUsernameClientFacade().requestNickname("Test2");

            for (int i = 0; i < 2 && nickname2 == null; i++) {
                TimeUnit.SECONDS.sleep(1);
            }

            assertEquals("Test2", nickname2);
            assertTrue(gameClient2.isConnected());
        }

        gameClient2.getLobbyFacade().joinLobby(lobbyLeaderId1);

        for (int i = 0; i < 2 && ((lobbyLeaderId2 == null || lobbyLeaderId2.equals(-1L)) || lobbyMembers2.isEmpty()); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(lobbyLeaderId1, lobbyLeaderId2);
        assertEquals(2, lobbyMembers2.size());
        assertTrue(lobbyMembers2.contains(nickname1) && lobbyMembers2.contains(nickname2));

        //todo: test member joined for 1st client

        gameClient2.getLobbyFacade().leaveLobby();
        for (int i = 0; i < 10 && !lobbyMembers2.isEmpty(); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(-1L, lobbyLeaderId2);
        assertTrue(lobbyMembers2.isEmpty());
    }

    @Test
    @Order(5)
    void leaveLobbyTest() throws Exception {
        gameClient.getLobbyFacade().leaveLobby();
        for (int i = 0; i < 10 && !lobbyLeaderId1.equals(-1L); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(-1L, lobbyLeaderId1);
        assertEquals(0, lobbyMembers1.size());
    }

    @Test
    @Order(6)
    void chatMessageTest() throws Exception {
        gameClient.getLobbyFacade().createLobby();

        TimeUnit.SECONDS.sleep(1);

        assertNotNull(lobbyLeaderId1);

        gameClient2.getLobbyFacade().joinLobby(lobbyLeaderId1);

        TimeUnit.SECONDS.sleep(1);

        assertNotNull(lobbyLeaderId2);

        gameClient.getChatMessageFacade().sendPlayerLobbyChatMessage(nickname1, "Test message");

        TimeUnit.SECONDS.sleep(1);

        assertEquals("Test", messageNickname2);
        assertEquals("Test message", message2);

        gameClient2.getChatMessageFacade().sendPlayerLobbyChatMessage(nickname2, "Test message 2");

        TimeUnit.SECONDS.sleep(1);

        assertEquals("Test2", messageNickname1);
        assertEquals("Test message 2", message1);

        gameClient.getLobbyFacade().leaveLobby();

        TimeUnit.SECONDS.sleep(1);

        assertEquals(-1L, lobbyLeaderId1);

        gameClient2.getLobbyFacade().leaveLobby();

        TimeUnit.SECONDS.sleep(1);

        assertEquals(-1L, lobbyLeaderId2);
    }

    @Test
    @Order(7)
    void startSessionTest() throws Exception {
        assertEquals(-1L, lobbyLeaderId1);
        assertEquals(-1L, lobbyLeaderId2);

        createLobbyTest();
        gameClient2.getLobbyFacade().joinLobby(lobbyLeaderId1);
        for(int i = 0; i < 2 && lobbyMembers2.isEmpty(); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(lobbyLeaderId1, lobbyLeaderId2);
//todo:        assertTrue(lobbyMembers1.contains(nickname1) && lobbyMembers1.contains(nickname2));
        assertTrue(lobbyMembers2.contains(nickname1) && lobbyMembers2.contains(nickname2));

        gameClient.getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertFalse(sessionMember1);

        gameClient2.getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertTrue(sessionMember1);
        assertTrue(sessionMember2);

        gameClient.getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);

        assertTrue(sessionMember1);
        assertTrue(sessionMember2);

        gameClient2.getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);

        assertFalse(sessionMember1);
        assertFalse(sessionMember2);
    }

    @Test
    @Order(8)
    void movingWithinSessionBoundsTest() throws Exception {
        assertFalse(sessionMember1);
        assertFalse(sessionMember2);

        gameClient.getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertFalse(sessionMember1);

        gameClient2.getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertTrue(sessionMember1);
        assertTrue(sessionMember2);

        gameClient.getStateClientFacade().requestState(10, 10);

        TimeUnit.MILLISECONDS.sleep(60);

        assertEquals(10, x1);
        assertEquals(10, y1);
        assertEquals(10, x2);
        assertEquals(10, y2);

        gameClient2.getStateClientFacade().requestState(11, 11);

        TimeUnit.MILLISECONDS.sleep(60);

        assertEquals(11, x2);
        assertEquals(11, y2);
        assertEquals(11, x1);
        assertEquals(11, y1);

        for (int i = 100; i < 200; i++) {
            gameClient.getStateClientFacade().requestState(i, Math.min(i , 600));

            TimeUnit.MILLISECONDS.sleep(60);

            assertEquals(i, x1);
            assertEquals(Math.min(i, 600), y1);
            assertEquals(i, x2);
            assertEquals(Math.min(i, 600), y2);

            gameClient2.getStateClientFacade().requestState(i, Math.min(i, 600));

            TimeUnit.MILLISECONDS.sleep(60);

            assertEquals(i, x2);
            assertEquals(Math.min(i, 600), y2);
            assertEquals(i, x1);
            assertEquals(Math.min(i, 600), y1);
        }

        gameClient.getSessionClientFacade().requestStopSession();
        gameClient2.getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(0);
        gameClient2.shutdownGracefullyAfterNSeconds(0);
        gameServer.shutdownGracefullyAfterNSeconds(0);
    }

    private void resetGameClientSingletonInstance() throws NoSuchFieldException, IllegalAccessException {
        final var singletonInstance = GameClient.class.getDeclaredField("INSTANCE");
        singletonInstance.setAccessible(true);
        singletonInstance.set(GameClient.class, null);
    }
}
