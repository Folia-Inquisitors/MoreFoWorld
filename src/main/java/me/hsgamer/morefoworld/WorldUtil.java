package me.hsgamer.morefoworld;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @see net.minecraft.server.MinecraftServer#loadWorld0(String)
 * @see CraftServer#createWorld(WorldCreator)
 */
public final class WorldUtil {
    public static FeedbackWorld addWorld(WorldCreator creator) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();

        String name = creator.name();

        String levelName = console.getProperties().levelName;
        ResourceKey<net.minecraft.world.level.Level> worldKey = null;
        if (name.equals(levelName)) {
            return Feedback.WORLD_DEFAULT.toFeedbackWorld();
        } else if (name.equals(levelName + "_nether")) {
            if (craftServer.getAllowNether()) {
                return Feedback.WORLD_DEFAULT.toFeedbackWorld();
            }
            worldKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            if (craftServer.getAllowEnd()) {
                return Feedback.WORLD_DEFAULT.toFeedbackWorld();
            }
            worldKey = net.minecraft.world.level.Level.END;
        }

        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), name);
        World world = craftServer.getWorld(name);

        CraftWorld worldByKey = (CraftWorld) craftServer.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            return world == worldByKey
                    ? Feedback.WORLD_ALREADY_EXISTS.toFeedbackWorld(worldByKey)
                    : Feedback.WORLD_DUPLICATED.toFeedbackWorld();
        }

        if ((folder.exists()) && (!folder.isDirectory())) {
            return Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
        }

        if (generator == null) {
            generator = craftServer.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension");
        };

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dynamic;
        if (worldSession.hasWorldData()) {
            net.minecraft.world.level.storage.LevelSummary worldinfo;

            try {
                dynamic = worldSession.getDataTag();
                worldinfo = worldSession.getSummary(dynamic);
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                LevelStorageSource.LevelDirectory convertable_b = worldSession.getLevelDirectory();

                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dynamic = worldSession.getDataTagFallback();
                    worldinfo = worldSession.getSummary(dynamic);
                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
                    return Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
                }

                worldSession.restoreLevelDataFromOld();
            }

            if (worldinfo.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
            }

            if (!worldinfo.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return Feedback.WORLD_FOLDER_INVALID.toFeedbackWorld();
            }
        } else {
            dynamic = null;
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata;
        WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
        RegistryAccess.Frozen iregistrycustom_dimension = worldloader_a.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> iregistry = iregistrycustom_dimension.registryOrThrow(Registries.LEVEL_STEM);
        if (dynamic != null) {
            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());

            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
            iregistrycustom_dimension = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings worldsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            worldsettings = new LevelSettings(name, getGameType(GameMode.SURVIVAL), hardcore, Difficulty.EASY, false, new GameRules(), worldloader_a.dataConfiguration());
            worlddimensions = properties.create(worldloader_a.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());

            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            iregistrycustom_dimension = worlddimensions_b.dimensionsRegistryAccess();
        }
        iregistry = iregistrycustom_dimension.registryOrThrow(Registries.LEVEL_STEM);
        worlddata.customDimensions = iregistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed());
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem worlddimension = iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value(), worlddimension.generator(), console.registryAccess());
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(
                    worldSession, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, iregistrycustom_dimension, console.options.has("recreateRegionFiles")
            );
        }

        if (worldKey == null) {
            worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(creator.key().namespace(), creator.key().value()));
        }

        if (creator.keepSpawnLoaded() == TriState.FALSE) {
            worlddata.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }
        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, console.progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, null, creator.environment(), generator, biomeProvider);

        console.addLevel(internal);

        int loadRegionRadius = ((32) >> 4);
        internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());
        for (int currX = -loadRegionRadius; currX <= loadRegionRadius; ++currX) {
            for (int currZ = -loadRegionRadius; currZ <= loadRegionRadius; ++currZ) {
                ChunkPos pos = new ChunkPos(currX, currZ);
                internal.chunkSource.addTicketAtLevel(
                        net.minecraft.server.level.TicketType.UNKNOWN, pos, ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager.MAX_TICKET_LEVEL, pos
                );
            }
        }

        internal.setSpawnSettings(true, true);

        console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
        io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(internal);

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld())); // Call Event

        return Feedback.SUCCESS.toFeedbackWorld(internal.getWorld());
    }

    public static GameType getGameType(GameMode gameMode) {
        return switch (gameMode) {
            case SURVIVAL -> GameType.SURVIVAL;
            case CREATIVE -> GameType.CREATIVE;
            case ADVENTURE -> GameType.ADVENTURE;
            case SPECTATOR -> GameType.SPECTATOR;
        };
    }

    public enum Feedback {
        WORLD_ALREADY_EXISTS,
        WORLD_DUPLICATED,
        WORLD_FOLDER_INVALID,
        WORLD_DEFAULT,
        SUCCESS;

        public FeedbackWorld toFeedbackWorld(CraftWorld world) {
            return new FeedbackWorld(world, this);
        }

        public FeedbackWorld toFeedbackWorld() {
            return new FeedbackWorld(this);
        }
    }

    public static class FeedbackWorld {
        public final CraftWorld world;
        public final Feedback feedback;

        public FeedbackWorld(CraftWorld world, Feedback feedback) {
            this.world = world;
            this.feedback = feedback;
        }

        public FeedbackWorld(Feedback feedback) {
            this(null, feedback);
        }
    }
}
