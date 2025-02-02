package me.hsgamer.morefoworld.config;

import com.google.common.collect.BiMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Map;
import java.util.Optional;

class WorldConfigUtil {
    static Optional<World> getLinkedWorld(World world, Map<String, String> worldMap) {
        return Optional.ofNullable(worldMap.get(world.getName())).map(Bukkit::getWorld);
    }

    static Optional<World> getLinkedWorld(World world, BiMap<String, String> worldBiMap) {
        Optional<World> linkedWorld = getLinkedWorld(world, (Map<String, String>) worldBiMap);
        if (linkedWorld.isPresent()) {
            return linkedWorld;
        }

        linkedWorld = getLinkedWorld(world, (Map<String, String>) worldBiMap.inverse());
        return linkedWorld;
    }
}
