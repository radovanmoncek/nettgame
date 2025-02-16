package container.game.docker.ship.factories;

import container.game.docker.ship.parents.codecs.Decoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;

import java.util.Map;
import java.util.function.Function;

public interface DecoderFactory extends Function<Map<Byte, Class<? extends ProtocolDataUnit>>, Decoder<? extends ProtocolDataUnit>> {}
