package me.hsgamer.morefoworld.config.converter;

import me.hsgamer.hscore.config.annotation.converter.Converter;
import me.hsgamer.morefoworld.config.object.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PositionConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new HashMap<>();
            rawMap.forEach((key, value) -> map.put(Objects.toString(key), value));
            return Position.deserialize(map);
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof Position position) {
            return position.serialize();
        }
        return null;
    }
}
