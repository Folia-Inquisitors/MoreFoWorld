package me.hsgamer.morefoworld.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.hscore.config.annotation.StickyValue;
import me.hsgamer.morefoworld.config.converter.WorldBiMapConverter;
import org.bukkit.World;

import java.util.Optional;

public interface PortalConfig {
    @ConfigPath(value = "nether", converter = WorldBiMapConverter.class)
    @StickyValue
    default BiMap<String, String> getNetherPortals() {
        return ImmutableBiMap.of();
    }

    void setNetherPortals(BiMap<String, String> netherPortals);

    default void linkNetherPortal(String fromWorld, String toWorld) {
        BiMap<String, String> netherPortals = getNetherPortals();
        netherPortals.put(fromWorld, toWorld);
        setNetherPortals(netherPortals);
    }

    default boolean unlinkNetherPortal(String world) {
        BiMap<String, String> netherPortals = getNetherPortals();
        if (netherPortals.containsKey(world)) {
            netherPortals.remove(world);
        } else if (netherPortals.containsValue(world)) {
            netherPortals.inverse().remove(world);
        } else {
            return false;
        }
        setNetherPortals(netherPortals);
        return true;
    }

    default Optional<World> getWorldFromNetherPortal(World world) {
        return WorldConfigUtil.getLinkedWorld(world, getNetherPortals());
    }

    @ConfigPath(value = "end", converter = WorldBiMapConverter.class)
    @StickyValue
    default BiMap<String, String> getEndPortals() {
        return ImmutableBiMap.of();
    }

    void setEndPortals(BiMap<String, String> endPortals);

    default void linkEndPortal(String fromWorld, String toWorld) {
        BiMap<String, String> endPortals = getEndPortals();
        endPortals.put(fromWorld, toWorld);
        setEndPortals(endPortals);
    }

    default boolean unlinkEndPortal(String world) {
        BiMap<String, String> endPortals = getEndPortals();
        if (endPortals.containsKey(world)) {
            endPortals.remove(world);
        } else if (endPortals.containsValue(world)) {
            endPortals.inverse().remove(world);
        } else {
            return false;
        }
        setEndPortals(endPortals);
        return true;
    }

    default Optional<World> getWorldFromEndPortal(World world) {
        return WorldConfigUtil.getLinkedWorld(world, getEndPortals());
    }
}
