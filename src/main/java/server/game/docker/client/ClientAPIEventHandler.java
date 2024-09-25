package server.game.docker.client;

@FunctionalInterface
public interface ClientAPIEventHandler<T> {
    void handle(T data);
}
