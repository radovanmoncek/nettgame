package container.game.docker.modules.examples.chat.models;

import container.game.docker.ship.parents.models.ProtocolDataUnit;

/**
 * Example chat message ProtocolDataUnit.
 */
public record ChatMessageProtocolDataUnit(String authorNick, String message) implements ProtocolDataUnit {}
