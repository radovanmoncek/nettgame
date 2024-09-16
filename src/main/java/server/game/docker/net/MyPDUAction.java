package server.game.docker.net;

@FunctionalInterface
public interface MyPDUAction {
    void perform(MyPDU packet);
}
