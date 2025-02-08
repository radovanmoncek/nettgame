package proxy.container.game.docker.ship.bootstrap;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

/**
 * <p>
 *     A reverse proxy Docker Container that forwards individual client requests to specific Instance Containers.
 *     This class also serves as a wrapper for the individual Docker Game Container instances.
 *     It communicates with the Docker Daemon, provisions the containers, and manages their operation / lifecycle.
 * </p>
 * <p>
 *     The default Docker Game Server port number is the port <b>4321</b>.
 * </p>
 */
public class ProxyContainer {

    public static void main(String[] args) {

    }
}
