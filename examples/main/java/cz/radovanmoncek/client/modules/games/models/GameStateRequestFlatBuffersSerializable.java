package cz.radovanmoncek.client.modules.games.models;

import cz.radovanmoncek.server.ship.compiled.schemas.GameStateRequest;
import cz.radovanmoncek.ship.parents.models.FlatBufferSerializable;

public record GameStateRequestFlatBuffersSerializable(int x, int y, int rotationAngle, String name, byte gameStatus, String gameCode) implements FlatBufferSerializable<GameStateRequest> {

    @Override
    public Class<GameStateRequest> getSchemaClass() {

        return GameStateRequest.class;
    }
}
