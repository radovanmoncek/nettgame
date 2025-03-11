package cz.radovanmoncek.server;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.junit.jupiter.api.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameServerDockerSystemTest {
    private static final String DOCKER_HOST = "tcp://localhost:2375";
    private static DockerClient dockerClient;
    private static CreateContainerResponse container;

    @BeforeAll
    static void setup() {

        final var defaultDockerClientConfig =
                DefaultDockerClientConfig
                        .createDefaultConfigBuilder()
                        .withDockerHost(DOCKER_HOST)
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
    }

    @Test
    void instanceContainerRunningTest(){

        final var inspectContainerCmd = dockerClient.inspectContainerCmd(container.getId());

        final var containerRunning = inspectContainerCmd
                .exec()
                .getState()
                .getRunning();

        assertTrue(Objects.requireNonNull(containerRunning));
    }

    @RepeatedTest(Integer.MAX_VALUE)
    void instanceContainerRunningIndefinitelyTest() throws InterruptedException {

        TimeUnit.SECONDS.sleep(10);
    }

    @AfterEach
    void cleanUpInstanceContainerIfStopped() {

        if(!Objects.requireNonNull(dockerClient.inspectContainerCmd(container.getId()).exec().getState().getRunning())) {

            dockerClient
                    .removeContainerCmd(container.getId())
                    .exec();

            System.exit(0);
        }
    }

    @AfterAll
    static void tearDown() throws Exception {

        dockerClient.stopContainerCmd(container.getId()).exec();
        dockerClient.removeContainerCmd(container.getId()).exec();
        dockerClient.close();
    }
}
