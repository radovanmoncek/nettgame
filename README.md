### Nettgame

is an extension of the excellent **Netty** framework that aims to provide tools as a framework for 
game server development.

#### Key points

- The developed server is meant for containerized runtime (Docker), but it does not have to be.
- Nettgame is written in the Java programming language, and utilizes FlatBuffers as its 
serialization library of choice.
- Persistence is handled through the well-known Java ORM framework Hibernate, enabling easy
extension, and scaling. The default DBMS (Data Base Management System) option is meant to be MySQL,
but, thanks to Hibernate, and JDBC, not the only possible one.
- Do be aware, that the only possible transport layer protocol option, currently, is **TCP**.

#### What nettgame can do?

- Support multiple game instances
- Transport data using FlatBuffers enabled codecs
- Hold stateful player data using Netty AttributeMap
- Persist data through the Hibernate framework
- Handle many incoming connections
- Run inside of a Docker container
- Be extended through the Porto software architecture

#### How to use?

- Get hold of a current version of the .jar artifact (releases)
- Import Nettgame .jar into your project
- Initialize a new NettgameServerBootstrap
- Add handlers to serve business logic of your networked game

#### Dependencies
```xml
        <!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
            <version>4.2.0.RC4</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.google.flatbuffers/flatbuffers-java -->
        <dependency>
            <groupId>com.google.flatbuffers</groupId>
            <artifactId>flatbuffers-java</artifactId>
            <version>25.2.10</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>7.0.0.Beta4</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.12.0</version>
        </dependency>
```
#### Sources, research and further reading

- https://mahmoudz.github.io/Porto/
- https://flatbuffers.dev/white_paper/
- https://hibernate.org/orm/
- https://dev.mysql.com/doc/refman/8.0/en/introduction.html
- https://app.docker.com/
- https://junit.org/junit5/
- https://netty.io/

#### UML Class diagram

![UML Class diagram](https://github.com/radovanmoncek/nettgame/blob/development/design/Nettgame_class_diagram.png)
