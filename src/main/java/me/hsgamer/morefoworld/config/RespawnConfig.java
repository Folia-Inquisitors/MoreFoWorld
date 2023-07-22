package me.hsgamer.morefoworld.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.hscore.config.annotation.StickyValue;
import me.hsgamer.morefoworld.config.converter.WorldBiMapConverter;
import org.bukkit.World;

import java.util.Optional;

public interface RespawnConfig {
    @ConfigPath(value = "worlds", converter = WorldBiMapConverter.class)
    @StickyValue
    default BiMap<String, String> getWorlds() {
        return ImmutableBiMap.of();
    }

    void setWorlds(BiMap<String, String> worlds);

    default void linkWorld(String fromWorld, String toWorld) {
        BiMap<String, String> netherPortals = getWorlds();
        netherPortals.put(fromWorld, toWorld);
        setWorlds(netherPortals);
    }

    default boolean unlinkWorld(String world) {
        BiMap<String, String> netherPortals = getWorlds();
        if (netherPortals.containsKey(world)) {
            netherPortals.remove(world);
        } else if (netherPortals.containsValue(world)) {
            netherPortals.inverse().remove(world);
        } else {
            return false;
        }
        setWorlds(netherPortals);
        return true;
    }

    default Optional<World> getRespawnWorld(World world) {
        return WorldConfigUtil.getLinkedWorld(world, getWorlds());
    }
}
