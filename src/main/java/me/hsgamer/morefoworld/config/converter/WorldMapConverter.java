package me.hsgamer.morefoworld.config.converter;

import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class WorldMapConverter implements Converter {
    @Override
    public Object convert(Object raw) {
        if (raw instanceof Map<?, ?> rawMap) {
            Map<String, String> worldMap = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> worldMap.put(Objects.toString(k), Objects.toString(v)));
            return worldMap;
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        return value;
    }
}
