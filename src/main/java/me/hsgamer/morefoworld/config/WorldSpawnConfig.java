package me.hsgamer.morefoworld.config;

import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.morefoworld.config.converter.PositionConverter;
import me.hsgamer.morefoworld.config.object.Position;
import me.hsgamer.morefoworld.config.object.WorldPosition;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface WorldSpawnConfig {
    @ConfigPath(value = "spawn", converter = PositionConverter.class)
    default Map<String, Position> getSpawn() {
        return new HashMap<>();
    }

    void setSpawn(Map<String, Position> spawn);

    default void setSpawn(WorldPosition worldPosition) {
        setSpawn(worldPosition.world(), worldPosition.position());
    }

    default void setSpawn(String world, Position position) {
        Map<String, Position> spawn = getSpawn();
        spawn.put(world, position);
        setSpawn(spawn);
    }

    default Optional<Position> getSpawn(World world) {
        return Optional.ofNullable(getSpawn().get(world.getName()));
    }
}
