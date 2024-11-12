module server.game.docker {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.common;

    exports server.game.docker;
    exports server.game.docker.modules.player.facades;
}