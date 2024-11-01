module server.game.docker {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.common;
    requires javafx.fxml;
    requires javafx.controls;

    exports server.game.docker;
}