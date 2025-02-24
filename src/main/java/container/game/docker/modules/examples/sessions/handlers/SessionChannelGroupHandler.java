package container.game.docker.modules.examples.sessions.handlers;

import container.game.docker.modules.examples.sessions.models.SessionFlag;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.examples.models.ExampleNetworkedGamePlayerSessionData;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;
import container.game.docker.ship.parents.models.PlayerSessionData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Example game session handler.
 */
public final class SessionChannelGroupHandler extends ChannelGroupHandler<SessionRequestProtocolDataUnit, SessionResponseProtocolDataUnit> {
    private static final Logger logger = LogManager.getLogger(SessionChannelGroupHandler.class);
    private static final int maxNickNameLength = 8, xBound = 2000, yBound = 2000, moveDelta = 8;
    private static final ConcurrentHashMap<UUID, SessionData> sessionData;
    private static final ConcurrentHashMap<String, UUID> shortToLongUUIDs;

    static {

        sessionData = new ConcurrentHashMap<>();
        shortToLongUUIDs = new ConcurrentHashMap<>();
    }

    @Override
    protected void playerChannelRead(SessionRequestProtocolDataUnit protocolDataUnit, PlayerSessionData playerSession) {

        playerChannelRead(protocolDataUnit, (ExampleNetworkedGamePlayerSessionData) playerSession);
    }

    private void playerChannelRead(final SessionRequestProtocolDataUnit sessionRequestProtocolDataUnit, final ExampleNetworkedGamePlayerSessionData playerSession) {

        final var playerChannelIdOptional = playerSession.retrievePlayerChannelId();

        if (playerChannelIdOptional.isEmpty()) {

            logger.error("Cannot process request, player channel id is empty, this should never happen");

            return;
        }

        final var playerChannelId = playerChannelIdOptional.get();

        final var playerSessionUUIDOptional = playerSession.retrieveSessionUUID();

        if (sessionRequestProtocolDataUnit.nickname().length() > maxNickNameLength) {

            unicastToClientChannel(
                    SessionResponseProtocolDataUnit.newINVALID(),
                    playerChannelId
            );

            logger.info("Invalid nickname length {}", sessionRequestProtocolDataUnit.nickname().length());

            return;
        }

        switch (sessionRequestProtocolDataUnit.sessionFlag()) {

            case START -> {

                logger.info("A player with ChannelId {} has requested a new username: {}", playerChannelId, sessionRequestProtocolDataUnit.nickname());

                logger.info("A player has requested session start {}", sessionRequestProtocolDataUnit);

                if (playerSession.retrieveSessionUUID().isPresent()) {

                    return;
                }

                playerSession.placeX(xBound / 2);
                playerSession.placeY(yBound / 2);
                playerSession.placeRotationAngle(0);
                playerSession.placeNickname(sessionRequestProtocolDataUnit.nickname());
                playerSession.placeWantsSessionToStop(false);

                createAndStartSession(new SessionHandler(), playerSession);
            }

            case STOP -> {

                logger.info("A player has requested session end {}", sessionRequestProtocolDataUnit);

                playerSession.placeWantsSessionToStop(true);
            }

            case JOIN -> {

                final var sessionUUID = shortToLongUUIDs.get(sessionRequestProtocolDataUnit.sessionUUID());

                if(sessionUUID == null) {

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                if (playerSessionUUIDOptional.isPresent() || !sessionData.containsKey(sessionUUID)) {

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                playerSession.placeSessionUUID(sessionUUID);
                playerSession.placeX(xBound / 2);
                playerSession.placeY(yBound / 2);
                playerSession.placeRotationAngle(0);
                playerSession.placeNickname(sessionRequestProtocolDataUnit.nickname());

                final var gameSession = sessionData.get(sessionUUID);

                if (gameSession == null || gameSession.player2Session != null){

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                gameSession.secondPlayerJoined = true;
                gameSession.player2Session = playerSession;

                logger.info("A player has requested session join {}", sessionUUID);
            }

            case STATE -> {

                final var xOptional = playerSession.retrieveX();
                final var yOptional = playerSession.retrieveY();
                final var rotationAngleOptional = playerSession.retrieveRotationAngle();

                if (xOptional.isEmpty() || yOptional.isEmpty() || rotationAngleOptional.isEmpty())
                    return;

                if (sessionRequestProtocolDataUnit.x() > xBound || sessionRequestProtocolDataUnit.y() > yBound){

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                if(sessionRequestProtocolDataUnit.x() < 0 || sessionRequestProtocolDataUnit.y() < 0) {

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                if(
                        sessionRequestProtocolDataUnit.rotationAngle() != 0
                                && sessionRequestProtocolDataUnit.rotationAngle() != 90
                                && sessionRequestProtocolDataUnit.rotationAngle() != 180
                                && sessionRequestProtocolDataUnit.rotationAngle() != 270
                ){

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                final var xDelta = Math.abs(sessionRequestProtocolDataUnit.x() - xOptional.get());
                final var yDelta = Math.abs(sessionRequestProtocolDataUnit.y() - yOptional.get());

                if ((xDelta != moveDelta && xDelta != 0) || (yDelta != 0 && yDelta != moveDelta)) {

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                playerSession.placeX(sessionRequestProtocolDataUnit.x());
                playerSession.placeY(sessionRequestProtocolDataUnit.y());
                playerSession.placeRotationAngle(sessionRequestProtocolDataUnit.rotationAngle());
            }
        }
    }

    @Override
    protected void playerDisconnected(PlayerSessionData playerSession) {
        playerDisconnected((ExampleNetworkedGamePlayerSessionData) playerSession);
    }

    private void playerDisconnected(final ExampleNetworkedGamePlayerSessionData playerSession) {

        playerSession.placeWantsSessionToStop(true);
    }

    private final class SessionHandler implements BiConsumer<SessionFlag, SessionData> {
        private int lastGameStateSum = 0;

        @Override
        public void accept(final SessionFlag sessionFlag, final SessionData sessionData) {

            final var player1Session = sessionData.player1Session;
            final var player2Session = Objects.requireNonNullElse(sessionData.player2Session, new ExampleNetworkedGamePlayerSessionData());

            final var sessionUUIDOptional = player1Session.retrieveSessionUUID();

            if (sessionUUIDOptional.isEmpty()) {

                return;
            }

            switch (sessionFlag) {

                case STATE -> {

                    if (player1Session.retrieveWantsSessionToStop().isPresent() && player1Session.retrieveWantsSessionToStop().get())
                        return;

                    final var x1Optional = player1Session.retrieveX();
                    final var y1Optional = player1Session.retrieveY();
                    final var rotationAngle1Optional = player1Session.retrieveRotationAngle();
                    final var player1ChannelIdOptional = player1Session.retrievePlayerChannelId();
                    final var x2Optional = player2Session.retrieveX();
                    final var y2Optional = player2Session.retrieveY();
                    final var rotationAngle2Optional = player2Session.retrieveRotationAngle();
                    final var player2ChannelIdOptional = player2Session.retrievePlayerChannelId();

                    if (
                            x1Optional.isEmpty()
                                    || y1Optional.isEmpty()
                                    || rotationAngle1Optional.isEmpty()
                                    || player1ChannelIdOptional.isEmpty()
                    ) {

                        return;
                    }

                    final var player1ChannelId = player1ChannelIdOptional.get();

                    if (
                            player2ChannelIdOptional.isEmpty()
                            || x2Optional.isEmpty()
                            || y2Optional.isEmpty()
                            || rotationAngle2Optional.isEmpty()
                    ) {

                        final var currentGameStateSum =
                                x1Optional.get()
                                        + y1Optional.get()
                                        + rotationAngle1Optional.get();

                        if (lastGameStateSum == currentGameStateSum)
                            return;

                        lastGameStateSum = currentGameStateSum;

                        final var sessionResponseProtocolDataUnit = SessionResponseProtocolDataUnit.newSTATE(
                                x1Optional.get(),
                                y1Optional.get(),
                                rotationAngle1Optional.get(),
                                0,
                                0,
                                0
                        );

                        unicastToClientChannel(sessionResponseProtocolDataUnit, player1ChannelId);

                        return;
                    }

                    final var currentGameStateSum =
                            x1Optional.get()
                            + y1Optional.get()
                            + rotationAngle1Optional.get()
                            + x2Optional.get()
                            + y2Optional.get()
                            + rotationAngle2Optional.get();

                    if(currentGameStateSum == lastGameStateSum)
                        return;

                    lastGameStateSum = currentGameStateSum;

                    final var player2ChannelId = player2ChannelIdOptional.get();
                    final var sessionResponseProtocolDataUnit = SessionResponseProtocolDataUnit.newSTATE(
                            x1Optional.get(),
                            y1Optional.get(),
                            rotationAngle1Optional.get(),
                            x2Optional.get(),
                            y2Optional.get(),
                            rotationAngle2Optional.get()
                    );

                    multicastToClientChannelIds(sessionResponseProtocolDataUnit, player1ChannelId, player2ChannelId);
                }

                case START -> {

                    final var xOptional = player1Session.retrieveX();
                    final var yOptional = player1Session.retrieveY();
                    final var rotationAngleOptional = player1Session.retrieveRotationAngle();
                    final var nicknameOptional = player1Session.retrieveNickname();
                    final var player1ChannelIdOptional = player1Session.retrievePlayerChannelId();

                    if (
                            xOptional.isEmpty()
                                    || yOptional.isEmpty()
                                    || rotationAngleOptional.isEmpty()
                                    || nicknameOptional.isEmpty()
                                    || player1ChannelIdOptional.isEmpty()
                    ) {

                        return;
                    }

                    final var player1ChannelId = player1ChannelIdOptional.get();

                    player1Session.placeSessionUUID(sessionUUIDOptional.get());

                    final var sessionResponseProtocolDataUnit = SessionResponseProtocolDataUnit.newSTART(
                            nicknameOptional.get(),
                            sessionUUIDOptional.get().toString().substring(0, 8)
                    );

                    unicastToClientChannel(sessionResponseProtocolDataUnit, player1ChannelId);
                }

                case STOP -> {

                    final var player1ChannelIdOptional = player1Session.retrievePlayerChannelId();

                    if (player1ChannelIdOptional.isEmpty())
                        return;

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newSTOP(), player1ChannelIdOptional.get());

                    player1Session.placeWantsSessionToStop(false);
                    player1Session.placeSessionUUID(null);
                }

                case JOIN -> {

                    final var player1ChannelIdOptional = player1Session.retrievePlayerChannelId();
                    final var player2ChannelIdOptional = player2Session.retrievePlayerChannelId();
                    final var nickname1Optional = player1Session.retrieveNickname();
                    final var nickname2Optional = player2Session.retrieveNickname();

                    if(
                            nickname1Optional.isEmpty()
                            || nickname2Optional.isEmpty()
                            || player1ChannelIdOptional.isEmpty()
                            || player2ChannelIdOptional.isEmpty()
                    )
                        return;

                    multicastToClientChannelIds(
                            SessionResponseProtocolDataUnit.newJOIN(nickname1Optional.get(), nickname2Optional.get()),
                            player1ChannelIdOptional.get(),
                            player2ChannelIdOptional.get()
                    );
                }

                default -> logger.error("Invalid session flag received from player: {}, this should never occur", sessionFlag);
            }
        }
    }

    private void createAndStartSession(
            final SessionHandler sessionAction,
            final ExampleNetworkedGamePlayerSessionData player1Session
    ) {

        final var sessionRunnable = new Runnable() {
            private final UUID sessionUUID = UUID.randomUUID();

            @Override
            public void run() {

                shortToLongUUIDs.put(sessionUUID.toString().substring(0, 8), sessionUUID);

                final var gameSession = new SessionData();

                gameSession.player1Session = player1Session;

                sessionData.put(sessionUUID, gameSession);

                var failureThreshold = 0;

                while (sessionData.get(sessionUUID) == null) {

                    try {

                        if (failureThreshold++ >= 250) {

                            logger.error("Session failure threshold exceeded 250 milliseconds");

                            return;
                        }

                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException interruptedException) {

                        logger.error(interruptedException.getMessage(), interruptedException);

                        return;
                    }
                }

                player1Session.placeSessionUUID(sessionUUID);
                
                sessionAction.accept(SessionFlag.START, gameSession);

                final var sessionStopConditionSupplier = (Supplier<Boolean>) () -> {

                    final var player1Session = gameSession.player1Session;
                    final var player2Session = gameSession.player2Session;

                    if(player1Session == null || player2Session == null)
                        return player1Session != null && player1Session.retrieveWantsSessionToStop().isPresent() && player1Session.retrieveWantsSessionToStop().get();

                    final var stop1 = player1Session.retrieveWantsSessionToStop();
                    final var stop2 = player2Session.retrieveWantsSessionToStop();

                    if(stop1.isEmpty() || stop2.isEmpty())
                        return false;

                    return stop1.get() && stop2.get();
                };

                while (!sessionStopConditionSupplier.get()) {

                    try {

                        if (gameSession.secondPlayerJoined) {

                            gameSession.secondPlayerJoined = false;

                            sessionAction.accept(SessionFlag.JOIN, gameSession);
                        }
                        
                        sessionAction.accept(SessionFlag.STATE, gameSession);
                        
                        TimeUnit.MILLISECONDS.sleep(33);
                    } catch (final InterruptedException interruptedException) {

                        logger.error(interruptedException.getMessage(), interruptedException);

                        player1Session.placeWantsSessionToStop(true);

                        break;
                    }
                }

                sessionAction.accept(SessionFlag.STOP, gameSession);

                sessionData.remove(sessionUUID);

                shortToLongUUIDs.remove(sessionUUID.toString().substring(0, 8));

                logger.info("Session {} has ended", sessionUUID);
            }
        };

        final var sessionThread = Executors
                .defaultThreadFactory()
                .newThread(sessionRunnable);

        sessionThread.setName(String.format("Game session %s", sessionRunnable.sessionUUID));
        sessionThread.start();

        logger.warn("Game Session {} Thread started", sessionRunnable.sessionUUID);
    }

    private static class SessionData {
        private boolean secondPlayerJoined;
        private ExampleNetworkedGamePlayerSessionData player1Session;
        private ExampleNetworkedGamePlayerSessionData player2Session;
    }
}
