### Nettgame

is an extension of the excellent Netty framework that aims to provide tools as a framework for 
game server development. 

#### Key points

- The developed server is meant for containerized runtime (Docker), but it does not have to be.

- Nettgame is written in the Java programming language, and utilizes FlatBuffers as its 
serialization library of choice.

- Persistence is handled through the well-known Java ORM framework Hibernate, enabling easy
extension, and scaling. The default DBMS (Data Base Management System) option is meant to be MySQL,
but, thanks to hibernate, and JDBC, not the only possible one.

- Do be aware, that the only possible transport layer protocol option, currently, is **TCP**.

#### What Nettgame can do

#### Nettgame in 5 minutes

- A Nettgame server is initialized through NettgameServerBootstrap

#### How to use

- Get hold of a current version of the .jar artifact

- Import Nettgame .jar into your project

#### Sources, and further reading / research

- 

#### Legal

The licence of Nettgame is the MIT licence.
