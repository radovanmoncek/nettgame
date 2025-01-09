package server.game.docker.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.junit.jupiter.api.*;
import server.game.docker.client.ship.bootstrap.GameClient;
import server.game.docker.client.ship.examples.SampleGameClient;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientConnectivityTest {
    private static DockerClient dockerClient;
    private static CreateContainerResponse container;
    private static SampleGameClient sampleGameClient1, sampleGameClient2;

    @BeforeAll
    static void setup() throws Exception {
        final var defaultDockerClientConfig =
                DefaultDockerClientConfig
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

        TimeUnit.SECONDS.sleep(2);

        sampleGameClient1 = new SampleGameClient();

        resetGameClientSingletonInstance();

        sampleGameClient2 = new SampleGameClient();

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    @Order(1)
    void connectionTest() {
        assertTrue(sampleGameClient1.getGameClient().isConnected());
    }

    @Test
    @Order(2)
    void usernameRequestAndReceiveTest() throws InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> sampleGameClient1.getGameClient().getUsernameClientFacade().requestNickname("TestThatIsWayOverTheLimitOf8Characters"));
        assertThrows(IllegalArgumentException.class, () -> sampleGameClient2.getGameClient().getUsernameClientFacade().requestNickname("TestThatIsWayOverTheLimitOf8Characters"));

        sampleGameClient1.getGameClient().getUsernameClientFacade().requestNickname("Test");

        for (int i = 0; i < 10 && sampleGameClient1.getNickname() == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals("Test", sampleGameClient1.getNickname());
    }

    @Test
    @Order(3)
    void createLobbyTest() throws Exception {
        sampleGameClient1.getGameClient().getLobbyFacade().createLobby();

        for (int i = 0; i < 10 && (sampleGameClient1.getLobbyLeaderId() == null || sampleGameClient1.getLobbyLeaderId().equals(-1L)); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(1, sampleGameClient1.getLobbyMembers().size());
        assertTrue(sampleGameClient1.getLobbyMembers().contains(sampleGameClient1.getNickname()));
    }

    @Order(4)
    @RepeatedTest(4)
    void joinLobbyTest() throws Exception {
        sampleGameClient2.getGameClient().getUsernameClientFacade().requestNickname("Test2");

        for (int i = 0; i < 2 && sampleGameClient2.getNickname() == null; i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals("Test2", sampleGameClient2.getNickname());

        assertTrue(sampleGameClient2.getGameClient().isConnected());

        sampleGameClient2.getGameClient().getLobbyFacade().joinLobby(sampleGameClient1.getLobbyLeaderId());

        for (int i = 0; i < 2 && ((sampleGameClient2.getLobbyLeaderId() == null || sampleGameClient2.getLobbyLeaderId().equals(-1L)) || sampleGameClient2.getLobbyMembers().isEmpty()); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(sampleGameClient1.getLobbyLeaderId(), sampleGameClient2.getLobbyLeaderId());

        assertEquals(2, sampleGameClient2.getLobbyMembers().size());

        assertTrue(sampleGameClient2.getLobbyMembers().contains(sampleGameClient1.getNickname()) && sampleGameClient2.getLobbyMembers().contains(sampleGameClient2.getNickname()));

        assertEquals(2, sampleGameClient1.getLobbyMembers().size());

        assertTrue(sampleGameClient1.getLobbyMembers().contains(sampleGameClient1.getNickname()) && sampleGameClient1.getLobbyMembers().contains(sampleGameClient2.getNickname()));

        sampleGameClient2.getGameClient().getLobbyFacade().leaveLobby();

        for (int i = 0; i < 10 && !sampleGameClient2.getLobbyMembers().isEmpty(); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(-1L, sampleGameClient2.getLobbyLeaderId());

        assertTrue(sampleGameClient2.getLobbyMembers().isEmpty());
    }

    @Test
    @Order(5)
    void leaveLobbyTest() throws Exception {
        sampleGameClient1.getGameClient().getLobbyFacade().leaveLobby();
        for (int i = 0; i < 10 && !sampleGameClient1.getLobbyLeaderId().equals(-1L); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(-1L, sampleGameClient1.getLobbyLeaderId());
        assertEquals(0, sampleGameClient1.getLobbyMembers().size());
    }

    @Test
    @Order(6)
    void chatMessageTest() throws Exception {
        sampleGameClient1.getGameClient().getLobbyFacade().createLobby();

        TimeUnit.SECONDS.sleep(1);

        assertNotNull(sampleGameClient1.getLobbyLeaderId());

        sampleGameClient2.getGameClient().getLobbyFacade().joinLobby(sampleGameClient1.getLobbyLeaderId());

        TimeUnit.SECONDS.sleep(1);

        assertNotNull(sampleGameClient2.getLobbyLeaderId());

        sampleGameClient1.getGameClient().getChatMessageFacade().sendPlayerLobbyChatMessage(sampleGameClient1.getNickname(), "Test message");

        TimeUnit.SECONDS.sleep(1);

        assertEquals("Test", sampleGameClient2.getChat().get(sampleGameClient2.getChat().size() - 1).get(0));
        assertEquals("Test message", sampleGameClient2.getChat().get(sampleGameClient2.getChat().size() - 1).get(1));

        sampleGameClient2.getGameClient().getChatMessageFacade().sendPlayerLobbyChatMessage(sampleGameClient2.getNickname(), "Test message 2");

        TimeUnit.SECONDS.sleep(1);

        assertEquals("Test2", sampleGameClient1.getChat().get(sampleGameClient1.getChat().size() - 1).get(0));
        assertEquals("Test message 2", sampleGameClient1.getChat().get(sampleGameClient1.getChat().size() - 1).get(1));

        sampleGameClient1.getGameClient().getLobbyFacade().leaveLobby();

        TimeUnit.SECONDS.sleep(1);

        assertEquals(-1L, sampleGameClient1.getLobbyLeaderId());

        sampleGameClient2.getGameClient().getLobbyFacade().leaveLobby();

        TimeUnit.SECONDS.sleep(1);

        assertEquals(-1L, sampleGameClient2.getLobbyLeaderId());
    }

    @Test
    @Order(7)
    void startSessionTest() throws Exception {
        assertEquals(-1L, sampleGameClient1.getLobbyLeaderId());
        assertEquals(-1L, sampleGameClient2.getLobbyLeaderId());

        createLobbyTest();

        sampleGameClient2.getGameClient().getLobbyFacade().joinLobby(sampleGameClient1.getLobbyLeaderId());

        for (int i = 0; i < 2 && sampleGameClient2.getLobbyMembers().isEmpty(); i++) {
            TimeUnit.SECONDS.sleep(1);
        }

        assertEquals(sampleGameClient1.getLobbyLeaderId(), sampleGameClient2.getLobbyLeaderId());

        assertTrue(sampleGameClient1.getLobbyMembers().contains(sampleGameClient1.getNickname()) && sampleGameClient1.getLobbyMembers().contains(sampleGameClient2.getNickname()));

        assertTrue(sampleGameClient2.getLobbyMembers().contains(sampleGameClient1.getNickname()) && sampleGameClient2.getLobbyMembers().contains(sampleGameClient2.getNickname()));

        sampleGameClient1.getGameClient().getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertFalse(sampleGameClient1.isSessionMember());

        sampleGameClient2.getGameClient().getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertTrue(sampleGameClient1.isSessionMember());
        assertTrue(sampleGameClient2.isSessionMember());

        sampleGameClient1.getGameClient().getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);

        sampleGameClient2.getGameClient().getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);

        assertFalse(sampleGameClient1.isSessionMember());
        assertFalse(sampleGameClient2.isSessionMember());
    }

    @Test
    @Order(8)
    void movingWithinSessionBoundsTest() throws Exception {
        assertFalse(sampleGameClient1.isSessionMember());
        assertFalse(sampleGameClient2.isSessionMember());

        sampleGameClient1.getGameClient().getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertFalse(sampleGameClient2.isSessionMember());

        sampleGameClient2.getGameClient().getSessionClientFacade().requestStartSession();

        TimeUnit.SECONDS.sleep(1);

        assertTrue(sampleGameClient1.isSessionMember());
        assertTrue(sampleGameClient2.isSessionMember());

        sampleGameClient1.getGameClient().getStateClientFacade().requestState(10, 10);

        TimeUnit.MILLISECONDS.sleep(40);

        assertEquals(10, sampleGameClient1.getGameEntities().get(0).x());
        assertEquals(10, sampleGameClient1.getGameEntities().get(0).y());
        assertEquals(0, sampleGameClient1.getGameEntities().get(1).x());
        assertEquals(0, sampleGameClient1.getGameEntities().get(1).y());

        sampleGameClient2.getGameClient().getStateClientFacade().requestState(11, 11);

        TimeUnit.MILLISECONDS.sleep(40);

        assertEquals(11, sampleGameClient2.getGameEntities().get(1).x());
        assertEquals(11, sampleGameClient2.getGameEntities().get(1).y());
        assertEquals(10, sampleGameClient2.getGameEntities().get(0).x());
        assertEquals(10, sampleGameClient2.getGameEntities().get(0).y());

        for (int i = 100; i < 200; i++) {
            sampleGameClient1.getGameClient().getStateClientFacade().requestState(i, i);

            TimeUnit.MILLISECONDS.sleep(40);

            assertEquals(i, sampleGameClient1.getGameEntities().get(0).x());
            assertEquals(i, sampleGameClient1.getGameEntities().get(0).y());

            sampleGameClient2.getGameClient().getStateClientFacade().requestState(i, 10);

            TimeUnit.MILLISECONDS.sleep(40);

            assertEquals(i, sampleGameClient2.getGameEntities().get(1).x());
            assertEquals(10, sampleGameClient2.getGameEntities().get(1).y());
        }

        sampleGameClient1.getGameClient().getSessionClientFacade().requestStopSession();
        sampleGameClient2.getGameClient().getSessionClientFacade().requestStopSession();

        TimeUnit.SECONDS.sleep(2);

        assertFalse(sampleGameClient1.isSessionMember());
        assertFalse(sampleGameClient2.isSessionMember());
    }

    @AfterAll
    static void tearDown() throws Exception {
        dockerClient.stopContainerCmd(container.getId()).exec();
        dockerClient.close();
        dockerClient = null;
    }

    private static void resetGameClientSingletonInstance() throws NoSuchFieldException, IllegalAccessException {
        final var singletonInstance = GameClient.class.getDeclaredField("INSTANCE");
        singletonInstance.setAccessible(true);
        singletonInstance.set(GameClient.class, null);
    }
}
