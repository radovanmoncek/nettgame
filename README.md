#### nettgame

is my longest running academic project to date.

Formerly built upon the Netty framework and the Java platform/programming language, nettgame is a game server toolset that gained a new direction after excellent suggestions from my new thesis supervisor, becoming more focused on game sessions, C/C++, and scaling through K8s/K3s.

The primary aim is to offer a platform for running thread-encapsulating stateful game sessions that run YOUR game business logic, consistently, and with stability. One session should serve many players, and one server should hold many sessions. Game sessions are separated from their state, which allows to migrate them accross Kubernetes Pods.

This framework focuses on networked games that require relatively fast server response, put in simple terms, are neither Chess, nor Counter-Strike, but rather Deep Rock Galactic.

#### Technological stack

uses the C native programming language. Altough it is very old by now, C has proven itself to be one of the most stable, reliable, and fast programming languages available today.

#### Ideology

of nettgame is to offer maximum possible freedom to projects built upon it. Simplicity is also a big driving factor, therefore, minimizing the number of dependencies is a major focus.

#### Repository contents

consist of all source files for nettgame, example demonstrational game server, example demonstrational game client, and academic papers about nettgame that simultaneously serve as a documentation/manual.

#### Sources, research, and further reading

``` bibtex
https://mahmoudz.github.io/Porto/
https://flatbuffers.dev/white_paper/
https://hibernate.org/orm/
https://dev.mysql.com/doc/refman/8.0/en/introduction.html
https://app.docker.com/
https://junit.org/junit5/
https://netty.io/
https://www.yegor256.com/
https://www.youtube.com/@TsodingDaily
```
