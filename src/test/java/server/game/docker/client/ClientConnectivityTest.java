package server.game.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.junit.jupiter.api.*;
import server.game.docker.client.modules.lobby.facades.LobbyClientFacade;
import server.game.docker.client.modules.messages.facades.ChatMessageClientFacade;
import server.game.docker.client.modules.player.facades.PlayerClientFacade;
import server.game.docker.client.modules.sessions.facades.SessionClientFacade;
import server.game.docker.client.modules.state.facades.StateClientFacade;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientConnectivityTest {
    private static GameClient gameClient, gameClient2;
    private static String nickname1, nickname2;
    private static Long lobbyLeaderId1, lobbyLeaderId2;
    private static Collection<String> lobbyMembers1, lobbyMembers2;
    private static boolean sessionMember1, sessionMember2;
    private static Integer x1, y1, x2, y2;
    private static String message1, message2;
    private static String messageNickname1, messageNickname2;
    private static DockerClient dockerClient;
    private static CreateContainerResponse container;

    @BeforeAll
    static void setup() throws Exception {
        final var defaultDockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        dockerClient = DockerClientImpl.getInstance(defaultDockerClientConfig, new ApacheDockerHttpClient.Builder().dockerHost(defaultDockerClientConfig.getDockerHost()).build());

        System.out.println(dockerClient.listImagesCmd().exec());

        dockerClient
                .startContainerCmd(
                (container = dockerClient
                        .createContainerCmd("docker-game-server:latest")
                        .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("4321:4321")))
                        .exec()
                )
                        .getId()
                )
                .exec();

        TimeUnit.SECONDS.sleep(10);

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

        gameClient.run(0, 10);
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

            gameClient2.run(0, 10);

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
            gameClient.getStateClientFacade().requestState(i, i);

            TimeUnit.MILLISECONDS.sleep(60);

            assertEquals(i, x1);
            assertEquals(i, y1);
            assertEquals(i, x2);
            assertEquals(i, y2);

            gameClient2.getStateClientFacade().requestState(i, i);

            TimeUnit.MILLISECONDS.sleep(60);

            assertEquals(i, x2);
            assertEquals(i, y2);
            assertEquals(i, x1);
            assertEquals(i, y1);
        }

        gameClient.getSessionClientFacade().requestStopSession();
        gameClient2.getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);
    }

    @AfterAll
    static void tearDown() throws Exception {
        gameClient.shutdownGracefullyAfterNSeconds(0);
        gameClient2.shutdownGracefullyAfterNSeconds(0);
        dockerClient.close();
        dockerClient.stopContainerCmd(container.getId());
    }

    private void resetGameClientSingletonInstance() throws NoSuchFieldException, IllegalAccessException {
        final var singletonInstance = GameClient.class.getDeclaredField("INSTANCE");
        singletonInstance.setAccessible(true);
        singletonInstance.set(GameClient.class, null);
    }
}
