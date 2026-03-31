package me.hsgamer.morefoworld.config.converter;

import me.hsgamer.hscore.config.annotation.converter.Converter;
import me.hsgamer.morefoworld.config.object.Position;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PositionMapConverter implements Converter {
    @Override
    public Map<String, Position> convert(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, Position> positionMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String worldName = Objects.toString(entry.getKey(), null);
                if (worldName == null) continue;
                if (entry.getValue() instanceof Map<?, ?> settingMap) {
                    Map<String, Object> mappedSetting = new HashMap<>();
                    settingMap.forEach((key, value) -> {
                        String keyString = Objects.toString(key, null);
                        if (keyString == null) return;
                        mappedSetting.put(keyString, value);
                    });
                    positionMap.put(worldName, Position.deserialize(mappedSetting));
                }
            }
            return positionMap;
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> convertToRaw(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Map<String, Object>> rawMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String worldName = Objects.toString(entry.getKey(), null);
                if (worldName == null) continue;
                if (entry.getValue() instanceof Position position) {
                    rawMap.put(worldName, position.serialize());
                }
            }
            return rawMap;
        }
        return null;
    }
}
