package container.game.docker.ship.factories;

import container.game.docker.ship.parents.codecs.Encoder;
import container.game.docker.ship.parents.models.ProtocolDataUnit;

import java.util.Map;
import java.util.function.Function;

public interface EncoderFactory extends Function<Map<Class<? extends ProtocolDataUnit>, Byte>, Encoder<? extends ProtocolDataUnit>> {}
