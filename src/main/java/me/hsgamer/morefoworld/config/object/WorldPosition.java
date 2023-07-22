package me.hsgamer.morefoworld.config.object;

import me.hsgamer.hscore.common.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class WorldPosition {
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;


    public WorldPosition(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static WorldPosition deserialize(Map<String, Object> map) {
        return new WorldPosition(
                Optional.ofNullable(map.get("world")).map(Object::toString).orElse(""),
                Optional.ofNullable(map.get("x")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("y")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("z")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("yaw")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::floatValue).orElse(0F),
                Optional.ofNullable(map.get("pitch")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::floatValue).orElse(0F)
        );
    }

    public static WorldPosition fromLocation(Location location) {
        return new WorldPosition(
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public Map<String, Object> serialize() {
        return Map.of(
                "world", world,
                "x", x,
                "y", y,
                "z", z,
                "yaw", yaw,
                "pitch", pitch
        );
    }
}
