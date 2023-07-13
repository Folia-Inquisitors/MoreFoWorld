package me.hsgamer.morefoworld.config.converter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.hsgamer.hscore.config.annotation.converter.Converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class WorldBiMapConverter implements Converter {
    @Override
    public BiMap<String, String> convert(Object raw) {
        if (raw instanceof Map<?, ?> worldMap) {
            BiMap<String, String> worldBiMap = HashBiMap.create();
            worldMap.forEach((k, v) -> worldBiMap.put(Objects.toString(k), Objects.toString(v)));
            return worldBiMap;
        }
        return null;
    }

    @Override
    public Object convertToRaw(Object value) {
        if (value instanceof BiMap<?, ?> worldBiMap) {
            Map<String, String> worldMap = new LinkedHashMap<>();
            worldBiMap.forEach((k, v) -> worldMap.put(Objects.toString(k), Objects.toString(v)));
            return worldMap;
        }
        return null;
    }
}
