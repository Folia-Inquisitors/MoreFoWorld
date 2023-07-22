package me.hsgamer.morefoworld.config;

import com.google.common.collect.BiMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Optional;

class WorldConfigUtil {
    static Optional<World> getLinkedWorld(World world, BiMap<String, String> worldMap) {
        String worldName = world.getName();
        Optional<String> optional = Optional.empty();
        if (worldMap.containsKey(worldName)) {
            optional = Optional.of(worldMap.get(worldName));
        } else if (worldMap.containsValue(worldName)) {
            optional = Optional.of(worldMap.inverse().get(worldName));
        }
        return optional.map(Bukkit::getWorld);
    }
}
