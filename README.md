# Nettgame

is an extension of the excellent Netty framework that aims to provide tools as a framework for
cz.radovanmoncek.test.suite.NettgameTest.game server development meant for containerized runtime (Docker).

Nettgame is written in the Java programming language, and utilizes FlatBuffers as its 
serialization library of choice.

Persistence is handled through the well-known Java ORM framework Hibernate, enabling easy
extension, and scaling. The default DBMS (Data Base Management System) option is meant to be MySQL,
but, thanks to hibernate, and JDBC, not the only possible one.

Do be aware, that the only possible transport layer protocol option, currently, is TCP.
