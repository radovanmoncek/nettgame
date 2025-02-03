package server.game.docker.ship.bootstrap;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

public class GameServer {
    private static final String dockerHost = "tcp://localhost:2375";
    private static DockerClient dockerClient;
    private static CreateContainerResponse container;

    public static void main(String[] args) {
        launchInstanceContainer();

        //todo: killInstanceContainer on closing operation
    }

    private static void launchInstanceContainer(){
        final var defaultDockerClientConfig =
                DefaultDockerClientConfig
                        .createDefaultConfigBuilder()
                        .withDockerHost(dockerHost)
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

    private static void killInstanceContainer() throws Exception {
        dockerClient.stopContainerCmd(container.getId()).exec();
        dockerClient.close();
        dockerClient = null;
    }
}
