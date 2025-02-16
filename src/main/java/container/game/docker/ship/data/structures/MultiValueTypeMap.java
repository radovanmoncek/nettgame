package container.game.docker.ship.data.structures;

import io.netty.channel.ChannelId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MultiValueTypeMap extends ConcurrentHashMap<String, Object> {

    public static MultiValueTypeMap of(final String key, Object ... values) {

        final var multiTypePropertyHashMap = new MultiValueTypeMap();

        List.of(values).forEach(value -> multiTypePropertyHashMap.put(key, value));

        return multiTypePropertyHashMap;
    }

    public Optional<Integer> getInteger(final String key) {

        if (get(key) instanceof Integer integerValue) {

            return Optional.of(integerValue);
        }

        return Optional.empty();
    }

    public Optional<String> getString(final String key) {

        if (get(key) instanceof String stringValue) {

            return Optional.of(stringValue);
        }

        return Optional.empty();
    }

    public static MultiValueTypeMap of(final String key, final Object value) {

        final var multiTypePropertyHashMap = new MultiValueTypeMap();

        multiTypePropertyHashMap.put(key, value);

        return multiTypePropertyHashMap;
    }

    public Optional<Boolean> getBoolean(final String key) {

        if(get(key) instanceof Boolean boolValue) {

            return Optional.of(boolValue);
        }

        return Optional.empty();
    }

    public Optional<ChannelId> getChannelId(final String key) {

        if(get(key) instanceof ChannelId channelIdValue) {

            return Optional.of(channelIdValue);
        }

        return Optional.empty();
    }

    public Optional<UUID> getUUID(final String key) {

        if(get(key) instanceof UUID uuidValue) {

            return Optional.of(uuidValue);
        }

        return Optional.empty();
    }

    public Optional<MultiValueTypeMap> getMultiTypePropertyHashMap(final String key) {

        if(get(key) instanceof MultiValueTypeMap multiValueTypeMap) {

            return Optional.of(multiValueTypeMap);
        }

        return Optional.empty();
    }
}
