#### nettgame

, formerly built upon the Netty framework and the Java platform/programming language, is a game server toolset focused on game sessions, C/C++, and scaling through K8s/K3s.

The primary aim is to offer a platform for running thread-encapsulating stateful game sessions that run YOUR game business logic, consistently, and with stability. One session should serve many players, and one server should hold many sessions. Game sessions are separated from their state, which allows to migrate them accross Kubernetes Pods.

This framework focuses on networked games that require relatively fast server response, put in simple terms, are neither Chess, nor Counter-Strike, but rather Deep Rock Galactic.

The p2p inter-Pod network, and its internal service protocol is inspired by the OSPF protocol.

#### Technological stack

uses the C native programming language for nettgame itself. Altough it is very old by now, C has proven itself to be one of the most stable, reliable, and fast programming languages available today. It is also the language most of the modern world technology relies on, be it a lesser known fact [9].

The demonstrational game server is built upon the C++ native language, and uses the Boost project as its sole dependency.

The example game client is built with the JavaScript scripting language, for prototyping reasons, and uses only the browser APIs.

#### Ideology

of nettgame is to offer maximum possible freedom to projects built upon it. This means that nettgame should only be as useful to you as possible, not dictate, how you should structure, or run your project. Simplicity is also a big driving factor, therefore, minimizing the number of dependencies is a major focus.

#### Repository contents

consist of all source files for nettgame, example demonstrational game server, example demonstrational game client, and academic papers about nettgame that simultaneously serve as a documentation/manual.

#### Sources, research, and further reading

``` bibtex
[1] https://mahmoudz.github.io/Porto/
[2] https://flatbuffers.dev/white_paper/
[3] https://hibernate.org/orm/
[4] https://dev.mysql.com/doc/refman/8.0/en/introduction.html
[5] https://app.docker.com/
[6] https://junit.org/junit5/
[7] https://netty.io/
[8] https://www.yegor256.com/
[9] https://www.youtube.com/@TsodingDaily
```
