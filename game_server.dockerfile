FROM openjdk:25
LABEL authors="Radovan Monček"
MAINTAINER Radovan Monček

COPY target/docker-game-server.jar gameserver.jar

EXPOSE 4321

ENTRYPOINT ["java", "-jar", "/gameserver.jar", "--mode", "containerized"]
