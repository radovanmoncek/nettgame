package server.game.docker.client;

import org.junit.jupiter.api.*;
import server.game.docker.GameServer;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientConnectivityTest {
    private static GameClient gameClient;
    private static GameServer gameServer;
    private static String nickname;
    private static Long lobbyLeaderId;
    private static Collection<String> lobbyMembers;
    private static Long lobbyLeaderIdJoinLobbyTest;

    @BeforeAll
    static void setup() throws Exception {
        gameServer = GameServer.newInstance();
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
                .withPlayerClientFacade(new PlayerClientFacade(){
                    @Override
                    public void receiveNewNickname(String newNickname) {
                        nickname = newNickname;
                    }
                })
                .withLobbyClientFacade(new LobbyClientFacade(){
                    @Override
                    public void receiveLobbyLeft(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyJoined(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = leaderId;
                        lobbyMembers = members;
                    }

                    @Override
                    public void receiveLobbyCreated(Long leaderId, Collection<String> members) {
                        lobbyLeaderId = lobbyLeaderIdJoinLobbyTest = leaderId;
                        lobbyMembers = members;
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

        for (int i = 0; i < 10 && nickname == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals("Test", nickname);
    }

    @Test
    @Order(3)
    void createLobbyTest() throws Exception {
        gameClient.getLobbyFacade().createLobby();
        for (int i = 0; i < 10 && lobbyLeaderId == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(1, lobbyMembers.size());
        assertTrue(lobbyMembers.contains(nickname));
        lobbyLeaderId = null;
        lobbyMembers = null;
    }

    @Test
    @Order(4)
    void leaveLobbyTest() throws Exception {
        gameClient.getLobbyFacade().leaveLobby();
        for (int i = 0; i < 10 && lobbyLeaderId == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(-1L, lobbyLeaderId);
        assertEquals(0, lobbyMembers.size());
        lobbyLeaderId = null;
        lobbyMembers = null;
    }

    @Test
    @Order(5)
    void joinLobbyTest() throws Exception {
        resetGameClientSingletonInstance();

        final var player2NickReceived = new AtomicBoolean(false);
        final var lobbyJoined = new AtomicBoolean(false);
        final var player2Nickname = new AtomicReference<>();
        final var player2LobbyLeaderId = new AtomicLong();
        final var player2LobbyMembers = new AtomicReference<Collection<String>>();
        final var gameClient2 = GameClient
                .newInstance()
                .withPlayerClientFacade(new PlayerClientFacade(){
                    @Override
                    public void receiveNewNickname(String newNickname) {
                        player2Nickname.set(newNickname);
                        player2NickReceived.set(true);
                    }
                })
                .withLobbyClientFacade(new LobbyClientFacade(){
                    @Override
                    public void receiveLobbyJoined(Long leaderChannelId, Collection<String> members) {
                        player2LobbyLeaderId.set(leaderChannelId);
                        player2LobbyMembers.set(members);
                        lobbyJoined.set(true);
                    }

                    @Override
                    public void receiveLobbyLeft(Long leaderId, Collection<String> members) {

                    }

                    @Override
                    public void receiveLobbyCreated(Long leaderId, Collection<String> members) {

                    }
                });

        gameClient2.run(0);

        assertTrue(gameClient2.isConnected());

        createLobbyTest();

        gameClient2.getUsernameClientFacade().requestNickname("Test2");
        gameClient2.getLobbyFacade().joinLobby(lobbyLeaderIdJoinLobbyTest);
        for (int i = 0; i < 20 && (!player2NickReceived.get() || !lobbyJoined.get()); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertTrue(player2NickReceived.get());
        assertTrue(lobbyJoined.get());
        assertEquals("Test2", player2Nickname.get());
        assertEquals(lobbyLeaderIdJoinLobbyTest, player2LobbyLeaderId.get());
        assertEquals(2, player2LobbyMembers.get().size());
        assertTrue(player2LobbyMembers.get().contains("Test") && player2LobbyMembers.get().contains("Test2"));

        gameClient2.shutdownGracefullyAfterNSeconds(0);
    }

    //TODO: messages (with lobby / session context) !!!!

    @Test
    @Order(6)
    void startSessionTest() throws Exception {
//        gameClient.getSessionClientFacade().requestStartSession();
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(0);
        gameServer.shutdownGracefullyAfterNSeconds(0);
    }

    private void resetGameClientSingletonInstance() throws NoSuchFieldException, IllegalAccessException {
        final var singletonInstance = GameClient.class.getDeclaredField("INSTANCE");
        singletonInstance.setAccessible(true);
        singletonInstance.set(GameClient.class, null);
    }
}
