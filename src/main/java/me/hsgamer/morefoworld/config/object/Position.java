package me.hsgamer.morefoworld.config.object;

import me.hsgamer.hscore.common.Validate;
import org.bukkit.Location;
import org.bukkit.World;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public record Position(double x, double y, double z, float yaw, float pitch) {
    public static Position deserialize(Map<String, Object> map) {
        return new Position(
                Optional.ofNullable(map.get("x")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("y")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("z")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::doubleValue).orElse(0D),
                Optional.ofNullable(map.get("yaw")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::floatValue).orElse(0F),
                Optional.ofNullable(map.get("pitch")).map(Object::toString).flatMap(Validate::getNumber).map(BigDecimal::floatValue).orElse(0F)
        );
    }

    public static Position fromLocation(Location location) {
        return new Position(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Map<String, Object> serialize() {
        return Map.of(
                "x", x,
                "y", y,
                "z", z,
                "yaw", yaw,
                "pitch", pitch
        );
    }
}
