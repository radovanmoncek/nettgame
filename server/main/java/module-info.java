open module cz.radovanmoncek.server {
    //thank you: https://stackoverflow.com/questions/46488346/error32-13-error-cannot-access-referenceable-class-file-for-javax-naming-re
    requires java.naming;
    requires java.desktop;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.common;
    requires flatbuffers.java;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
}
