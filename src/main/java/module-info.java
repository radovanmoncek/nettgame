module server.game.docker {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.common;
    requires java.desktop;
    requires com.github.dockerjava.core;
    requires com.github.dockerjava.transport.httpclient5;
    requires com.github.dockerjava.api;
    requires guava;

    exports server.game.docker.modules.player.facades;
    exports server.game.docker.modules.chat.facades;
    exports server.game.docker.modules.state.facades;
    exports server.game.docker.modules.lobby.facades;
    exports server.game.docker.modules.session.facades;
    exports server.game.docker.ship.bootstrap;
    exports server.game.docker.ship.examples;
}