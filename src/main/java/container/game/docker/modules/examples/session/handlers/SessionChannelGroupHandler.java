package container.game.docker.modules.examples.session.handlers;

import container.game.docker.modules.examples.session.models.SessionFlag;
import container.game.docker.modules.examples.session.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.session.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.ProtocolDataUnit;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Example game session.
 */
public class SessionChannelGroupHandler extends ChannelGroupHandler<SessionRequestProtocolDataUnit, SessionResponseProtocolDataUnit> {
    public static final int MAX_NICKNAME_LENGTH = 8;
    public static final Integer x_bound = 800, y_bound = 600;

    @Override
    protected void playerChannelRead(final SessionRequestProtocolDataUnit sessionRequestProtocolDataUnit, final ChannelId playerChannelId) {

        switch (sessionRequestProtocolDataUnit.sessionFlag()) {

            case START -> {

                System.out.printf("A client with ChannelId %s has requested a new username: %s\n", playerChannelId, sessionRequestProtocolDataUnit.nickname()); //todo: log4j

                System.out.printf("A player has requested session start %s \n", sessionRequestProtocolDataUnit); //todo: log4j

                if (sessionHash == null) {
                    return;
                }

                startManagingSession((sessionHash, sessionFlag) -> {

                    switch (sessionFlag) {

                        case STATE -> {

                            final var gameSession = sessionStateTable.get(sessionHash);

                            final var sessionResponseProtocolDataUnit = new SessionResponseProtocolDataUnit(
                                    SessionFlag.STATE,
                                    sessionHash,
                                    gameSession.player1.x(),
                                    gameSession.player1.y(),
                                    gameSession.player1.rotationAngle(),
                                    gameSession.player2.x(),
                                    gameSession.player2.y(),
                                    gameSession.player2.rotationAngle(),
                                    gameSession.player1.nickname,
                                    gameSession.player2.nickname
                            );

                            multicastToClientChannelIds(sessionResponseProtocolDataUnit, gameSession.player1.channelId, gameSession.player2.channelId);

                            return !sessionStateTable.get(sessionHash).stopped;
                        }

                        case START -> {

                            this.sessionHash = sessionHash;

                            sessionStateTable.putIfAbsent(
                                    sessionHash,
                                    new GameSession(
                                            false,
                                            new Player(
                                                    playerChannelId,
                                                    sessionRequestProtocolDataUnit.x(),
                                                    sessionRequestProtocolDataUnit.y(),
                                                    sessionRequestProtocolDataUnit.rotationAngle(),
                                                    sessionRequestProtocolDataUnit.nickname()
                                            ),
                                            null
                                    )
                            );

                            return true;
                        }

                        default -> {return true;}
                    }
                });

                unicastToClientChannel(
                        new SessionResponseProtocolDataUnit(
                                SessionFlag.START,
                                sessionHash,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                sessionRequestProtocolDataUnit.nickname(),
                                null
                        ),
                        playerChannelId
                );
            }

            case STOP -> {

                if (!sessionRequestProtocolDataUnit.sessionHash().equals(sessionHash))
                    return;

                System.out.printf("A player has requested session end %s \n", sessionRequestProtocolDataUnit); //todo: log4j

                sessionStateTable.compute(sessionHash, (sessionHash, currentGameSession) -> new GameSession(true, Objects.requireNonNull(currentGameSession).player1, Objects.requireNonNull(currentGameSession).player2));
            }

            case JOIN -> {

                if (sessionRequestProtocolDataUnit.sessionHash().equals(sessionHash))
                    return;

//                todo:
            }

            case STATE -> {

                if (!sessionRequestProtocolDataUnit.sessionHash().equals(sessionHash))
                    return;

                if (sessionRequestProtocolDataUnit.x() > x_bound || sessionRequestProtocolDataUnit.y() > y_bound)
                    return;

                final var currentGameSession = sessionStateTable.get(sessionHash);

                final var player1 = playerChannelId.equals(currentGameSession.player1.channelId) ? new Player(
                        playerChannelId,
                        sessionRequestProtocolDataUnit.x(),
                        sessionRequestProtocolDataUnit.y(),
                        sessionRequestProtocolDataUnit.rotationAngle(),
                        currentGameSession.player1.nickname()
                ) : currentGameSession.player1;

                final var player2 = playerChannelId.equals(currentGameSession.player2.channelId) ? new Player(
                        playerChannelId,
                        sessionRequestProtocolDataUnit.x(),
                        sessionRequestProtocolDataUnit.y(),
                        sessionRequestProtocolDataUnit.rotationAngle(),
                        currentGameSession.player2.nickname()
                ) : currentGameSession.player2;

                final var gameSession = new GameSession(
                        false,
                        player1,
                        player2
                );

                sessionStateTable.put(sessionRequestProtocolDataUnit.sessionHash(), gameSession);

                final var sessionResponseProtocolDataUnit = new SessionResponseProtocolDataUnit(
                        SessionFlag.STATE,
                        sessionHash,
                        gameSession.player1.x,
                        gameSession.player1.y,
                        gameSession.player1.rotationAngle,
                        gameSession.player2.x,
                        gameSession.player2.y,
                        gameSession.player2.rotationAngle,
                        gameSession.player1.nickname,
                        gameSession.player2.nickname
                );

                multicastToClientChannelIds(sessionResponseProtocolDataUnit, gameSession.player1.channelId, gameSession.player2.channelId);
            }
        }

    }

    @Override
    protected void playerDisconnected(ChannelId id) {

    }

    private record GameSession(Boolean stopped, Player player1, Player player2) {}

    private record Player(ChannelId channelId, Integer x, Integer y, Integer rotationAngle, String nickname) {}
}
