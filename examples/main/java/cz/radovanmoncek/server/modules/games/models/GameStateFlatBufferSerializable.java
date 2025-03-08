package cz.radovanmoncek.server.modules.games.models;

import cz.radovanmoncek.server.ship.compiled.schemas.GameState;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

/**
 * "Band-aid" class for FlatBuffers Schema.
 */
public record GameStateFlatBufferSerializable(Game game, Player[] players) implements FlatBufferSerializable<GameState> {

    @Override
    public Class<GameState> getSchemaClass() {

        return GameState.class;
    }

    public record Game(byte gameStatus, String gameCode) {}

    public record Player(int x, int y, int rotationAngle, String name) {}
}
