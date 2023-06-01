package me.hsgamer.morefoworld.config.converter;

import me.hsgamer.hscore.config.annotation.converter.Converter;
import me.hsgamer.morefoworld.WorldSetting;

import java.util.*;

public class WorldSettingListConverter implements Converter {
    @Override
    public List<WorldSetting> convert(Object raw) {
        if (raw instanceof Map<?, ?> worldMap) {
            List<WorldSetting> worldSettings = new ArrayList<>();
            for (Map.Entry<?, ?> entry : worldMap.entrySet()) {
                String worldName = Objects.toString(entry.getKey(), null);
                if (worldName == null) continue;
                if (entry.getValue() instanceof Map<?, ?> settingMap) {
                    Map<String, Object> mappedSettingMap = new HashMap<>();
                    settingMap.forEach((key, value) -> {
                        String keyString = Objects.toString(key, null);
                        if (keyString == null) return;
                        mappedSettingMap.put(keyString, value);
                    });
                    worldSettings.add(WorldSetting.fromMap(worldName, mappedSettingMap));
                }
            }
            return worldSettings;
        }
        return null;
    }

    @Override
    public Map<String, Map<String, Object>> convertToRaw(Object value) {
        if (value instanceof List<?> list) {
            Map<String, Map<String, Object>> rawMap = new HashMap<>();
            for (Object object : list) {
                if (object instanceof WorldSetting worldSetting) {
                    rawMap.put(worldSetting.getName(), worldSetting.toMap());
                }
            }
            return rawMap;
        }
        return null;
    }
}
