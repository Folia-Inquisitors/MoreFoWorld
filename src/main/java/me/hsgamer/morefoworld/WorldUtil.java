package me.hsgamer.morefoworld;

import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager;
import ca.spottedleaf.moonrise.patches.chunk_system.scheduling.NewChunkHolder;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.JsonOps;
import io.papermc.paper.threadedregions.RegionizedServer;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.world.PaperWorldLoader;
import io.papermc.paper.world.migration.WorldFolderMigration;
import me.hsgamer.hscore.task.BatchRunnable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorldUtil {
    private static final VarHandle RS_INSTANCE;
    private static final VarHandle RS_WORLDS;
    private static final VarHandle CW_WORLDS;

    static {
        try {
            var rLookup = MethodHandles.privateLookupIn(RegionizedServer.class, MethodHandles.lookup());
            var cLookup = MethodHandles.privateLookupIn(CraftServer.class, MethodHandles.lookup());
            RS_INSTANCE = rLookup.findStaticVarHandle(RegionizedServer.class, "INSTANCE", RegionizedServer.class);
            RS_WORLDS = rLookup.findVarHandle(RegionizedServer.class, "worlds", CopyOnWriteArrayList.class);
            CW_WORLDS = cLookup.findVarHandle(CraftServer.class, "worlds", Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize VarHandles for reflection", e);
        }
    }

    /**
     * Add a world to the server.
     *
     * @param creator the world creator
     * @return the feedback
     * @see CraftServer#createWorld(WorldCreator) Folia stubs this with "not implemented properly yet".
     * This method bypasses the stub by calling NMS internals directly.
     */
    public static FeedbackWorld addWorld(WorldCreator creator) {
        String name = creator.name();
        if (Bukkit.getWorld(name) != null || Bukkit.getWorld(creator.key()) != null) {
            return Feedback.WORLD_ALREADY_EXISTS.toFeedbackWorld();
        }
        try {
            World world = addWorld0(creator);
            return Feedback.SUCCESS.toFeedbackWorld(world);
        } catch (Throwable throwable) {
            return Feedback.ERROR.toFeedbackWorld(throwable);
        }
    }

    /**
     * Internal world creation. Mirrors the startup sequence used by
     * {@link net.minecraft.server.dedicated.DedicatedServer#initServer()}.
     *
     * @param creator the world creator
     * @return the created world
     * @see net.minecraft.server.MinecraftServer#createLevel(LevelStem, io.papermc.paper.world.PaperWorldLoader.WorldLoadingInfoAndData, LevelDataAndDimensions.WorldDataAndGenSettings)
     * @see DedicatedServer#initServer()
     */
    private static World addWorld0(WorldCreator creator) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();

        String name = creator.name();
        ChunkGenerator chunkGenerator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();

        if (chunkGenerator == null) {
            chunkGenerator = craftServer.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        RegistryAccess registryAccess = console.registryAccess();
        final ResourceKey<net.minecraft.world.level.Level> dimensionKey = CraftNamespacedKey.toResourceKey(Registries.DIMENSION, creator.key());
        net.minecraft.core.Registry<LevelStem> levelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        final LevelStem configuredStem = levelStemRegistry.getValue(actualDimension);
        if (configuredStem == null) {
            throw new IllegalStateException("Missing configured level stem " + actualDimension);
        }
        try {
            WorldFolderMigration.migrateApiWorld(
                    console.storageSource,
                    registryAccess,
                    name,
                    actualDimension,
                    dimensionKey
            );
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to migrate legacy world " + name, ex);
        }
        PaperWorldLoader.LoadedWorldData loadedWorldData = PaperWorldLoader.loadWorldData(
                console,
                dimensionKey,
                name
        );
        final PrimaryLevelData primaryLevelData = (PrimaryLevelData) console.getWorldData();
        WorldGenSettings worldGenSettings = LevelStorageSource.readExistingSavedData(console.storageSource, dimensionKey, registryAccess, WorldGenSettings.TYPE)
                .result()
                .orElse(null);
        RegistryAccess contextRegistryAccess = registryAccess;
        if (worldGenSettings == null) {
            WorldOptions worldOptions = new WorldOptions(creator.seed(), creator.generateStructures(), creator.bonusChest());

            String flatGenSettings = creator.generatorSettings();
            if (flatGenSettings.isEmpty()) {
                flatGenSettings = FlatLevelGeneratorSettings.CODEC.encodeStart(registryAccess.createSerializationContext(JsonOps.INSTANCE), FlatLevelGeneratorSettings.getDefault(
                        registryAccess.lookupOrThrow(Registries.BIOME),
                        registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
                        registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                )).getOrThrow().toString();
            }
            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse(flatGenSettings), creator.type().name().toLowerCase(Locale.ROOT));
            WorldDimensions worldDimensions = properties.create(registryAccess);

            WorldDimensions.Complete complete = worldDimensions.bake(levelStemRegistry);
            if (complete.dimensions().getValue(actualDimension) == null) {
                throw new IllegalStateException("Missing generated level stem " + actualDimension + " for world " + name);
            }

            worldGenSettings = new WorldGenSettings(worldOptions, worldDimensions);
            contextRegistryAccess = complete.dimensionsRegistryAccess();
            loadedWorldData.levelOverrides().setHardcore(creator.hardcore());
            loadedWorldData = new PaperWorldLoader.LoadedWorldData(
                    loadedWorldData.bukkitName(),
                    loadedWorldData.uuid(),
                    loadedWorldData.pdc(),
                    loadedWorldData.levelOverrides()
            );
        }
        final WorldGenSettings genSettingsFinal = worldGenSettings;

        levelStemRegistry = contextRegistryAccess.lookupOrThrow(Registries.LEVEL_STEM);

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(console.storageSource, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, contextRegistryAccess, console.options.has("recreateRegionFiles"));
        }

        long biomeZoomSeed = BiomeManager.obfuscateSeed(genSettingsFinal.options().seed());
        LevelStem customStem = genSettingsFinal.dimensions().get(actualDimension).orElse(null);
        if (customStem == null) {
            customStem = levelStemRegistry.getValue(actualDimension);
        }
        if (customStem == null) {
            throw new IllegalStateException("Missing level stem for world " + name + " using key " + actualDimension);
        }

        WorldInfo worldInfo = new CraftWorldInfo(loadedWorldData.bukkitName(), CraftNamespacedKey.fromMinecraft(dimensionKey.identifier()), genSettingsFinal.options().seed(), primaryLevelData.enabledFeatures(), creator.environment(), customStem.type().value(), customStem.generator(), registryAccess, loadedWorldData.uuid());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        final SavedDataStorage savedDataStorage = new SavedDataStorage(console.storageSource.getDimensionPath(dimensionKey).resolve(LevelResource.DATA.id()), console.getFixerUpper(), registryAccess);
        savedDataStorage.set(WorldGenSettings.TYPE, new WorldGenSettings(genSettingsFinal.options(), genSettingsFinal.dimensions()));
        List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(savedDataStorage)
        );

        ServerLevel serverLevel = new ServerLevel(
                console,
                Util.backgroundExecutor(),
                console.storageSource,
                genSettingsFinal,
                dimensionKey,
                customStem,
                primaryLevelData.isDebugWorld(),
                biomeZoomSeed,
                creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(),
                true,
                actualDimension,
                creator.environment(),
                chunkGenerator,
                biomeProvider,
                savedDataStorage,
                loadedWorldData
        );

        console.addLevel(serverLevel);
        console.initWorld(serverLevel, creator);

        serverLevel.setSpawnSettings(true);

        console.prepareLevel(serverLevel);

        return serverLevel.getWorld();
    }

    public static void applyWorldSpawn(Location location) {
        location.getWorld().setSpawnLocation(location);
    }

    /**
     * Schedule the world to be unloaded asynchronously.
     * <p>
     * Uses {@link BatchRunnable} across three stages:
     * <ol>
     *   <li>Gather all chunk holders from {@link ChunkHolderManager#getChunkHolders()}</li>
     *   <li>Save each chunk on its owning region thread via {@link org.bukkit.Bukkit#getRegionScheduler()}</li>
     *   <li>Final cleanup on the global tick thread via {@link GlobalRegionScheduler}</li>
     * </ol>
     *
     * @param plugin the plugin initiating the unload
     * @param world  the world to unload
     * @param save   whether to save chunks before removing
     * @return {@link Feedback#SUCCESS} if the unload was scheduled,
     * or an error feedback if pre-checks failed
     * @see CraftServer#unloadWorld(World, boolean) Folia stubs this with "not implemented properly yet".
     * This method bypasses the stub.
     * @see io.papermc.paper.threadedregions.RegionShutdownThread The only existing world cleanup in Folia.
     * Stage 1 mirrors its per-region chunk save logic adapted for a single-world context.
     * @see ChunkHolderManager#close(boolean, boolean) Stage 2 uses this to flush I/O and close caches.
     * @see ChunkHolderManager#getChunkHolders() Stage 0 snapshots loaded chunks from this.
     * @see NewChunkHolder#save(boolean) Stage 1 saves each holder on its owning region thread.
     */
    public static Feedback removeWorld(Plugin plugin, World world, boolean save) {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();
        CraftWorld craftWorld = (CraftWorld) world;
        ServerLevel level = craftWorld.getHandle();

        if (Bukkit.getWorld(world.getName()) == null) {
            return Feedback.WORLD_NOT_FOUND;
        }
        if (level.dimension() == Level.OVERWORLD) {
            return Feedback.CANNOT_UNLOAD_OVERWORLD;
        }
        if (!level.players().isEmpty()) {
            return Feedback.PLAYERS_ONLINE;
        }
        WorldUnloadEvent event = new WorldUnloadEvent(world);
        if (!event.callEvent()) {
            return Feedback.UNLOAD_CANCELLED;
        }

        ChunkHolderManager holderManager = ((ChunkSystemServerLevel) level).moonrise$getChunkTaskScheduler().chunkHolderManager;

        BatchRunnable batch = new BatchRunnable();

        // Stage 0: snapshot holders
        batch.addTaskPool(0, pool -> pool.addLast(process -> {
            process.getData().put("holders", holderManager.getChunkHolders());
            process.next();
        }));

        // Stage 1: save each chunk on its owning region thread
        batch.addTaskPool(1, pool -> pool.addLast(process -> {
            List<NewChunkHolder> holders = process.getData().get("holders");
            if (holders.isEmpty()) {
                process.next();
                return;
            }
            AtomicInteger remaining = new AtomicInteger(holders.size());
            for (NewChunkHolder h : holders) {
                int cx = h.chunkX;
                int cz = h.chunkZ;
                Bukkit.getRegionScheduler().execute(plugin, world, cx, cz, () -> {
                    try {
                        if (save) {
                            h.save(false);
                        }
                    } catch (Exception ignored) {
                        // chunk may have been unloaded concurrently
                    }
                    if (remaining.decrementAndGet() == 0) {
                        process.next();
                    }
                });
            }
        }));

        // Stage 2: final cleanup on the global tick thread
        // Must use halt=true to wait for in-flight I/O before closing caches
        batch.addTaskPool(2, pool -> pool.addLast(process -> {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> {
                try {
                    level.saveLevelData(true);
                    console.removeLevel(level);
                    removeFromRegionizedWorlds(level);
                    removeFromCraftWorlds(craftServer, world);
                    holderManager.close(save, true);
                } catch (Exception e) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error during world unload cleanup for " + world.getName(), e);
                }
                process.next();
            });
        }));

        batch.setTimeout(60, TimeUnit.SECONDS);

        Bukkit.getAsyncScheduler().runNow(plugin, _ -> {
            batch.run();
            if (batch.isTimeout()) {
                plugin.getLogger().warning("World unload for " + world.getName() + " timed out after 60s");
            } else {
                plugin.getLogger().info("World " + world.getName() + " unloaded");
            }
        });

        return Feedback.SUCCESS;
    }

    /**
     * Remove the world from {@link RegionizedServer#getInstance()}.
     * Folia's {@link RegionizedServer} has no public {@code removeWorld()} method,
     * so this uses a {@link VarHandle} on the private {@code worlds} field.
     *
     * @see RegionizedServer#addWorld(ServerLevel)
     */
    private static void removeFromRegionizedWorlds(ServerLevel level) {
        RegionizedServer rs = (RegionizedServer) RS_INSTANCE.get();
        @SuppressWarnings("unchecked")
        CopyOnWriteArrayList<ServerLevel> worlds = (CopyOnWriteArrayList<ServerLevel>) RS_WORLDS.get(rs);
        worlds.remove(level);
    }

    /**
     * Remove the world from {@link CraftServer}'s internal world map.
     * The {@code worlds} field is private, so this uses a {@link VarHandle}.
     *
     * @see CraftServer#addWorld(org.bukkit.World)
     */
    private static void removeFromCraftWorlds(CraftServer craftServer, World world) {
        @SuppressWarnings("unchecked")
        Map<String, World> worlds = (Map<String, World>) CW_WORLDS.get(craftServer);
        worlds.remove(world.getName().toLowerCase(Locale.ROOT));
    }

    public enum Feedback {
        WORLD_ALREADY_EXISTS,
        ERROR,
        SUCCESS,
        WORLD_NOT_FOUND,
        CANNOT_UNLOAD_OVERWORLD,
        PLAYERS_ONLINE,
        UNLOAD_CANCELLED;

        public FeedbackWorld toFeedbackWorld(World world) {
            return new FeedbackWorld(world, this, null);
        }

        public FeedbackWorld toFeedbackWorld() {
            return new FeedbackWorld(this, null);
        }

        public FeedbackWorld toFeedbackWorld(Throwable throwable) {
            return new FeedbackWorld(this, throwable);
        }
    }

    public record FeedbackWorld(World world, Feedback feedback, Throwable throwable) {
        public FeedbackWorld(Feedback feedback, Throwable throwable) {
            this(null, feedback, throwable);
        }
    }
}
