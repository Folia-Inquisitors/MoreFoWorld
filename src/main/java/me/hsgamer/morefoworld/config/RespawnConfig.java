package me.hsgamer.morefoworld.config;

import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.hscore.config.annotation.StickyValue;
import me.hsgamer.morefoworld.config.converter.WorldMapConverter;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface RespawnConfig {
    @ConfigPath(value = "worlds", converter = WorldMapConverter.class)
    @StickyValue
    default Map<String, String> getWorlds() {
        return new HashMap<>();
    }

    void setWorlds(Map<String, String> worlds);

    default void linkWorld(String fromWorld, String toWorld) {
        Map<String, String> worldMap = getWorlds();
        worldMap.put(fromWorld, toWorld);
        setWorlds(worldMap);
    }

    default boolean unlinkWorld(String world) {
        Map<String, String> linkMap = getWorlds();
        if (!linkMap.containsKey(world)) return false;
        linkMap.remove(world);
        setWorlds(linkMap);
        return true;
    }

    default Optional<World> getRespawnWorld(World world) {
        return WorldConfigUtil.getLinkedWorld(world, getWorlds());
    }
}
