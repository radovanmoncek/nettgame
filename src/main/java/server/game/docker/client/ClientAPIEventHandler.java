package server.game.docker.client;

import server.game.docker.net.dto.JoinLobbyReq;

@FunctionalInterface
public interface ClientAPIEventHandler<T> {
    void handle(T data);
}
