package me.hsgamer.morefoworld.config;

import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.morefoworld.config.converter.WorldPositionConverter;
import me.hsgamer.morefoworld.config.object.WorldPosition;

public interface SpawnConfig {
    @ConfigPath(value = "position", converter = WorldPositionConverter.class)
    default WorldPosition getPosition() {
        return new WorldPosition("world", 0, 100, 0, 0, 0);
    }

    void setPosition(WorldPosition position);

    @ConfigPath("enabled")
    default boolean isEnabled() {
        return false;
    }

    @ConfigPath("first-join")
    default boolean isFirstJoin() {
        return false;
    }
}
