package server.game.docker.client;

import server.game.docker.net.parents.pdus.PDU;

@FunctionalInterface
public interface ClientAPIEventHandler<T extends PDU> {
    void handle(T data);
}
