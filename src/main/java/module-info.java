module container.game.docker {
    requires io.netty.buffer;
    requires io.netty.transport;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.common;
    requires com.github.dockerjava.core;
    requires com.github.dockerjava.transport.httpclient5;
    requires com.github.dockerjava.api;
    requires guava;
    requires javassist;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires org.hibernate.orm.core;
    requires jdk.compiler;
    requires jakarta.persistence;
    requires flatbuffers.java;
}
