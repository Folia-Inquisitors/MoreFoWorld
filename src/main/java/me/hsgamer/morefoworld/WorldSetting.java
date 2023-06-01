package me.hsgamer.morefoworld;

import com.google.common.base.Enums;
import me.hsgamer.hscore.common.Validate;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class WorldSetting {
    private final String name;
    private long seed = 0L;
    private World.Environment environment = World.Environment.NORMAL;
    private String generator = "";
    private String biomeProvider = "";
    private WorldType type = WorldType.NORMAL;
    private boolean hardcore = false;
    private boolean generateStructures = true;
    private String generatorSettings = "";

    public WorldSetting(String name) {
        this.name = name;
    }

    public static WorldSetting edit(WorldSetting setting, Map<String, Object> map) {
        Optional.ofNullable(map.get("seed")).map(Object::toString).flatMap(Validate::getNumber).map(Number::longValue).ifPresent(setting::setSeed);
        Optional.ofNullable(map.get("environment")).map(Object::toString).flatMap(s -> Enums.getIfPresent(World.Environment.class, s.toUpperCase(Locale.ROOT)).toJavaUtil()).ifPresent(setting::setEnvironment);
        Optional.ofNullable(map.get("generator")).map(Object::toString).ifPresent(setting::setGenerator);
        Optional.ofNullable(map.get("biome-provider")).map(Object::toString).ifPresent(setting::setBiomeProvider);
        Optional.ofNullable(map.get("type")).map(Object::toString).flatMap(s -> Enums.getIfPresent(WorldType.class, s.toUpperCase(Locale.ROOT)).toJavaUtil()).ifPresent(setting::setType);
        Optional.ofNullable(map.get("hardcore")).map(Object::toString).map(Boolean::parseBoolean).ifPresent(setting::setHardcore);
        Optional.ofNullable(map.get("generate-structures")).map(Object::toString).map(Boolean::parseBoolean).ifPresent(setting::setGenerateStructures);
        Optional.ofNullable(map.get("generator-settings")).map(Object::toString).ifPresent(setting::setGeneratorSettings);
        return setting;
    }

    public static WorldSetting fromMap(String name, Map<String, Object> map) {
        return edit(new WorldSetting(name), map);
    }

    public WorldCreator toWorldCreator() {
        WorldCreator worldCreator = WorldCreator.name(name);
        if (seed != 0L) {
            worldCreator.seed(seed);
        }
        worldCreator.environment(environment);
        if (!generator.isEmpty()) {
            worldCreator.generator(generator);
        }
        if (!biomeProvider.isEmpty()) {
            worldCreator.biomeProvider(biomeProvider);
        }
        worldCreator.type(type);
        worldCreator.hardcore(hardcore);
        worldCreator.generateStructures(generateStructures);
        if (!generatorSettings.isEmpty()) {
            worldCreator.generatorSettings(generatorSettings);
        }
        return worldCreator;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "seed", seed,
                "environment", environment.name(),
                "generator", generator,
                "biome-provider", biomeProvider,
                "type", type.name(),
                "hardcore", hardcore,
                "generate-structures", generateStructures,
                "generator-settings", generatorSettings
        );
    }

    public String getName() {
        return name;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(World.Environment environment) {
        this.environment = environment;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getBiomeProvider() {
        return biomeProvider;
    }

    public void setBiomeProvider(String biomeProvider) {
        this.biomeProvider = biomeProvider;
    }

    public WorldType getType() {
        return type;
    }

    public void setType(WorldType type) {
        this.type = type;
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public boolean isGenerateStructures() {
        return generateStructures;
    }

    public void setGenerateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
    }

    public String getGeneratorSettings() {
        return generatorSettings;
    }

    public void setGeneratorSettings(String generatorSettings) {
        this.generatorSettings = generatorSettings;
    }
}
