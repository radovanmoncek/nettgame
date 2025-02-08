module container.game.docker {
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
    requires javassist;
}
