package me.hsgamer.morefoworld.config.object;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record WorldPosition(String world, Position position) {
    public static WorldPosition deserialize(Map<String, Object> map) {
        return new WorldPosition(
                Optional.ofNullable(map.get("world")).map(Object::toString).orElse(""),
                Position.deserialize(map)
        );
    }

    public static WorldPosition fromLocation(Location location) {
        return new WorldPosition(
                location.getWorld().getName(),
                Position.fromLocation(location)
        );
    }

    public Location toLocation() {
        return position.toLocation(Bukkit.getWorld(world));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", world);
        map.putAll(position.serialize());
        return map;
    }
}
