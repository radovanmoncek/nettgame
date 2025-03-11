FROM openjdk:17-jdk-alpine
LABEL authors="Radovan Monček"
MAINTAINER: https://github.com/radovanmoncek

COPY target/docker-game-server.jar dockergameserver.jar

EXPOSE 4321

ENTRYPOINT ["java", "-jar", "/dockergameserver.jar"]
