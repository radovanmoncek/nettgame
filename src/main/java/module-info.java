module server.game.docker {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.common;

    opens server.game.docker.client.examples.gui to javafx.controls, javafx.fxml, javafx.graphics;
}