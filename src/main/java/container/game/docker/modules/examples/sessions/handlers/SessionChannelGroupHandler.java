package container.game.docker.modules.examples.sessions.handlers;

import container.game.docker.modules.examples.sessions.models.SessionFlag;
import container.game.docker.modules.examples.sessions.models.SessionRequestProtocolDataUnit;
import container.game.docker.modules.examples.sessions.models.SessionResponseProtocolDataUnit;
import container.game.docker.ship.examples.concurrent.SubHandler;
import container.game.docker.ship.data.structures.MultiValueTypeMap;
import container.game.docker.ship.examples.functions.TriFunction;
import container.game.docker.ship.parents.handlers.ChannelGroupHandler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Example game session handler.
 */
public final class SessionChannelGroupHandler extends ChannelGroupHandler<SessionRequestProtocolDataUnit, SessionResponseProtocolDataUnit> {
    private static final int maxNickNameLength = 8, xBound = 800, yBound = 600, moveDelta = 4;
    private static final ConcurrentHashMap<UUID, MultiValueTypeMap> sessionData;
    private static final ThreadGroup subHandlers;
    private static final ConcurrentHashMap<String, UUID> shortToLongUUIDs;

    static {

        sessionData = new ConcurrentHashMap<>();
        shortToLongUUIDs = new ConcurrentHashMap<>();
        subHandlers = new ThreadGroup("Game Sessions");
    }

    @Override
    protected void playerChannelRead(final SessionRequestProtocolDataUnit sessionRequestProtocolDataUnit, final MultiValueTypeMap playerSession) {

        final var playerChannelIdOptional = playerSession.getChannelId("playerChannelId");

        if (playerChannelIdOptional.isEmpty()) {

            System.err.println("Cannot process request, player channel id is empty"); //todo: log4j

            return;
        }

        final var playerChannelId = playerChannelIdOptional.get();

        final var playerSessionUUIDOptional = playerSession.getUUID("sessionUUID");

        if (sessionRequestProtocolDataUnit.nickname().length() > maxNickNameLength) {

            unicastToClientChannel(
                    SessionResponseProtocolDataUnit.newINVALID(),
                    playerChannelId
            );

            System.err.println("Invalid nickname length"); //todo: log4j

            return;
        }

        switch (sessionRequestProtocolDataUnit.sessionFlag()) {

            case START -> {

                System
                        .out
                        .printf(
                                "A client with ChannelId %s has requested a new username: %s\n",
                                playerSession.get("playerChannelId"),
                                sessionRequestProtocolDataUnit.nickname()
                        ); //todo: log4j

                System.out.printf("A player has requested session start %s \n", sessionRequestProtocolDataUnit); //todo: log4j

                if (playerSession.containsKey("sessionUUID")) {

                    return;
                }

                playerSession.put("x", 0);
                playerSession.put("y", 0);
                playerSession.put("rotationAngle", 0);
                playerSession.put("nickname", sessionRequestProtocolDataUnit.nickname());

                createAndStartSession(new SessionHandler(), playerSession);
            }

            case STOP -> {

                System.out.printf("A player has requested session end %s \n", sessionRequestProtocolDataUnit); //todo: log4j

                playerSession.put("sessionStopped", true);
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

                playerSession.put("sessionUUID", sessionUUID);
                playerSession.put("x", 0);
                playerSession.put("y", 0);
                playerSession.put("rotationAngle", 0);
                playerSession.put("nickname", sessionRequestProtocolDataUnit.nickname());

                final var gameSession = sessionData.get(sessionUUID);

                if (gameSession == null || gameSession.containsKey("player2Session")){

                    unicastToClientChannel(SessionResponseProtocolDataUnit.newINVALID(), playerChannelId);

                    return;
                }

                gameSession.put("player2Session", playerSession);
                gameSession.put("secondPlayerJoined", true);

                System.out.println("A player has requested session join " + sessionUUID); // todo: log4j
            }

            case STATE -> {

                final var xOptional = playerSession.getInteger("x");
                final var yOptional = playerSession.getInteger("y");
                final var rotationAngleOptional = playerSession.getInteger("rotationAngle");

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

                playerSession.put("x", sessionRequestProtocolDataUnit.x());
                playerSession.put("y", sessionRequestProtocolDataUnit.y());
                playerSession.put("rotationAngle", sessionRequestProtocolDataUnit.rotationAngle());
            }
        }
    }

    @Override
    protected void playerDisconnected(final MultiValueTypeMap playerSession) {

        playerSession.put("sessionStopped", true);
    }

    private final class SessionHandler implements TriFunction<MultiValueTypeMap, MultiValueTypeMap, SessionFlag, Boolean> {
        private int lastGameStateSum = 0;

        @Override
        public Boolean apply(final MultiValueTypeMap player1Session, final MultiValueTypeMap player2Session, final SessionFlag sessionFlag) {

            final var sessionUUIDOptional = player1Session.getUUID("sessionUUID");

            if (sessionUUIDOptional.isEmpty()) {

                return true;
            }

            switch (sessionFlag) {

                case STATE -> {

                    if (player1Session.getBoolean("sessionStopped").orElse(false))
                        return true;

                    final var x1Optional = player1Session.getInteger("x");
                    final var y1Optional = player1Session.getInteger("y");
                    final var rotationAngle1Optional = player1Session.getInteger("rotationAngle");
                    final var player1ChannelIdOptional = player1Session.getChannelId("playerChannelId");
                    final var x2Optional = player2Session.getInteger("x");
                    final var y2Optional = player2Session.getInteger("y");
                    final var rotationAngle2Optional = player2Session.getInteger("rotationAngle");
                    final var player2ChannelIdOptional = player2Session.getChannelId("playerChannelId");

                    if (
                            x1Optional.isEmpty()
                                    || y1Optional.isEmpty()
                                    || rotationAngle1Optional.isEmpty()
                                    || player1ChannelIdOptional.isEmpty()
                    ) {

                        return false;
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
                            return false;

                        lastGameStateSum = currentGameStateSum;

                        final var sessionResponseProtocolDataUnit = SessionResponseProtocolDataUnit.newSTATE(
                                x1Optional.get(),
                                y1Optional.get(),
                                rotationAngle1Optional.get(),
                                0,
                                0,
                                0
                        );

                        SessionChannelGroupHandler.this.unicastToClientChannel(sessionResponseProtocolDataUnit, player1ChannelId);

                        return player1Session
                                .getBoolean("sessionStopped")
                                .orElse(false);
                    }

                    final var currentGameStateSum =
                            x1Optional.get()
                            + y1Optional.get()
                            + rotationAngle1Optional.get()
                            + x2Optional.get()
                            + y2Optional.get()
                            + rotationAngle2Optional.get();

                    if(currentGameStateSum == lastGameStateSum)
                        return false;

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

                    SessionChannelGroupHandler.this.multicastToClientChannelIds(sessionResponseProtocolDataUnit, player1ChannelId, player2ChannelId);

                    return !player1Session
                            .getBoolean("sessionStopped")
                            .orElse(false)
                            && player2Session
                            .getBoolean("sessionStopped")
                            .orElse(false);
                }

                case START -> {

                    final var xOptional = player1Session.getInteger("x");
                    final var yOptional = player1Session.getInteger("y");
                    final var rotationAngleOptional = player1Session.getInteger("rotationAngle");
                    final var nicknameOptional = player1Session.getString("nickname");
                    final var player1ChannelIdOptional = player1Session.getChannelId("playerChannelId");

                    if (
                            xOptional.isEmpty()
                                    || yOptional.isEmpty()
                                    || rotationAngleOptional.isEmpty()
                                    || nicknameOptional.isEmpty()
                                    || player1ChannelIdOptional.isEmpty()
                    ) {

                        return false;
                    }

                    final var player1ChannelId = player1ChannelIdOptional.get();

                    player1Session.put("sessionUUID", sessionUUIDOptional.get());

                    final var sessionResponseProtocolDataUnit = SessionResponseProtocolDataUnit.newSTART(
                            nicknameOptional.get(),
                            sessionUUIDOptional.get().toString().substring(0, 8)
                    );

                    SessionChannelGroupHandler.this.unicastToClientChannel(sessionResponseProtocolDataUnit, player1ChannelId);

                    return false;
                }

                case STOP -> {

                    final var player1ChannelIdOptional = player1Session.getChannelId("playerChannelId");

                    if (player1ChannelIdOptional.isEmpty())
                        return false;

                    SessionChannelGroupHandler.this.unicastToClientChannel(SessionResponseProtocolDataUnit.newSTOP(), player1ChannelIdOptional.get());

                    player1Session.put("sessionStopped", false);
                    player1Session.remove("sessionUUID");

                    return true;
                }

                case JOIN -> {

                    final var player1ChannelIdOptional = player1Session.getChannelId("playerChannelId");
                    final var player2ChannelIdOptional = player2Session.getChannelId("playerChannelId");
                    final var nickname1Optional = player1Session.getString("nickname");
                    final var nickname2Optional = player2Session.getString("nickname");

                    if(
                            nickname1Optional.isEmpty()
                            || nickname2Optional.isEmpty()
                            || player1ChannelIdOptional.isEmpty()
                            || player2ChannelIdOptional.isEmpty()
                    )
                        return false;

                    multicastToClientChannelIds(
                            SessionResponseProtocolDataUnit.newJOIN(nickname1Optional.get(), nickname2Optional.get()),
                            player1ChannelIdOptional.get(),
                            player2ChannelIdOptional.get()
                    );

                    return false;
                }

                default -> {

                    System.err.println("Invalid session flag: " + sessionFlag); //todo: log4j

                    return false;
                }
            }
        }
    }

    private void createAndStartSession(
            final TriFunction<MultiValueTypeMap, MultiValueTypeMap, SessionFlag, Boolean> isStopped,
            final MultiValueTypeMap player1Session
    ) {

        final var subHandler = new SubHandler() {
            private final UUID sessionUUID = UUID.randomUUID();

            @Override
            public String supplyName() {

                return String.format("Game Session Thread %s", sessionUUID);
            }

            @Override
            public void run() {

                shortToLongUUIDs.put(sessionUUID.toString().substring(0, 8), sessionUUID);

                final var gameSession = MultiValueTypeMap.of("player1Session", player1Session, new MultiValueTypeMap());

                sessionData.put(sessionUUID, gameSession);

                var failureThreshold = 0;

                while (sessionData.get(sessionUUID) == null) {

                    try {

                        if (failureThreshold++ >= 250)
                            return;

                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException interruptedException) {

                        interruptedException.printStackTrace(); //todo: log4j

                        return;
                    }
                }

                player1Session.put("sessionUUID", sessionUUID);
                player1Session.put(
                        "sessionStopped",
                        isStopped
                                .apply(
                                        player1Session,
                                        gameSession
                                                .getMultiTypePropertyHashMap("player2Session")
                                                .orElse(new MultiValueTypeMap()),
                                        SessionFlag.START
                                )
                );

                while (
                        !isStopped
                                .apply(
                                        player1Session,
                                        gameSession.getMultiTypePropertyHashMap("player2Session").orElse(new MultiValueTypeMap()),
                                        SessionFlag.STATE
                                )
                ) {

                    try {

                        final var secondPlayerJoined = gameSession.getBoolean("secondPlayerJoined");

                        if (secondPlayerJoined.isPresent() && secondPlayerJoined.get()) {

                            gameSession.remove("secondPlayerJoined");

                            isStopped.apply(
                                    player1Session,
                                    gameSession.getMultiTypePropertyHashMap("player2Session").orElse(new MultiValueTypeMap()),
                                    SessionFlag.JOIN
                            );
                        }

                        TimeUnit.MILLISECONDS.sleep(33);
                    } catch (final InterruptedException interruptedException) {

                        interruptedException.printStackTrace(); //todo: log4j

                        player1Session.put("sessionStopped", true);

                        break;
                    }
                }

                player1Session.put(
                        "sessionStopped",
                        isStopped.apply(
                                player1Session,
                                sessionData.get(sessionUUID).getMultiTypePropertyHashMap("player2Session").orElse(new MultiValueTypeMap()),
                                SessionFlag.STOP
                        )
                );

                sessionData.remove(sessionUUID);

                shortToLongUUIDs.remove(sessionUUID.toString().substring(0, 8));

                System.out.printf("Session %s has ended\n", sessionUUID); //todo: log4j
            }
        };

        final var sessionThread = new Thread(subHandlers, subHandler);

        sessionThread.setName(subHandler.supplyName());
        sessionThread.start();

        System.out.println("Game Session " + subHandler.sessionUUID + " Thread started"); //todo: log4j
    }
}
