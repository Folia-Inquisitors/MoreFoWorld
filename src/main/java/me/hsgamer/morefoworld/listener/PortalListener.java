package me.hsgamer.morefoworld.listener;

import ca.spottedleaf.concurrentutil.util.Priority;
import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.PortalConfig;
import me.hsgamer.morefoworld.config.WorldSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Portal teleporting to the linked worlds, done with the portal code of the server itself.
 * <p>
 * The portal process of the entity is registered with {@link Entity#setAsInsidePortal}, so the server keeps
 * driving the delay, the decay, the cooldown and the entry position through {@link Entity#handlePortal} and
 * {@link PortalProcessor#processPortalTeleportation}. The teleport of a linked {@link LinkedPortal} is
 * requested through {@link Portal#portalAsync}, the same hook the server uses for its own portals
 * ({@link NetherPortalBlock#portalAsync} and {@link net.minecraft.world.level.block.EndPortalBlock#portalAsync}).
 * <p>
 * A portal is identified by its {@link Material}, so a portal of a future version is picked up by
 * {@link #vanillaPortals} and left to the server until the plugin knows a link for it.
 */
public class PortalListener implements ListenerComponent {
    /**
     * The margin of {@link Entity#findOrCreatePortalAsync}: the extra chunks so that the final search for a
     * portal frame does not load any chunk.
     */
    private static final int SEARCH_CHUNK_MARGIN = 32;

    /**
     * The portal of the server for a {@link Material}, empty if the block is not a portal.
     */
    private final ConcurrentHashMap<Material, Optional<Portal>> vanillaPortals = new ConcurrentHashMap<>();
    /**
     * The portal of a link for a {@link Material}.
     */
    private final ConcurrentHashMap<Material, LinkedPortal> portals = new ConcurrentHashMap<>();
    private final BasePlugin plugin;
    private DebugComponent debug;

    public PortalListener(BasePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BasePlugin getPlugin() {
        return plugin;
    }

    @Override
    public void load() {
        debug = plugin.get(DebugComponent.class);
    }

    /**
     * The portal of the block, {@link net.minecraft.world.level.block.Blocks#NETHER_PORTAL} and
     * {@link net.minecraft.world.level.block.Blocks#END_PORTAL} are the portals of the server.
     *
     * @param material the material of the block
     * @return the portal, or empty if the block is not a portal
     */
    private static Optional<Portal> vanillaPortal(Material material) {
        if (!material.isBlock()) return Optional.empty();
        Block block = ((CraftBlockData) material.createBlockData()).getState().getBlock();
        return block instanceof Portal portal ? Optional.of(portal) : Optional.empty();
    }

    /**
     * Replaces the portal process of {@link NetherPortalBlock#entityInside} and
     * {@link net.minecraft.world.level.block.EndPortalBlock#entityInside} with a portal that knows the linked
     * world, the server then does the rest: {@link Entity#canUsePortal} decides whether the entity can portal,
     * {@link EntityPortalEnterEvent} is the event of the server and {@link Entity#setAsInsidePortal} starts the
     * portal process. Without a link the portal of the server is left alone.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityInsidePortal(EntityInsideBlockEvent event) {
        Material material = event.getBlock().getType();
        Portal vanilla = vanillaPortals.computeIfAbsent(material, PortalListener::vanillaPortal).orElse(null);
        if (vanilla == null) return;

        Entity entity = ((CraftEntity) event.getEntity()).getHandle();
        if (!entity.canUsePortal(false)) return;
        if (targetLevel((ServerLevel) entity.level(), material) == null) return;

        // the portal of the server only knows the default dimensions, the linked portal takes over
        event.setCancelled(true);

        Location location = event.getBlock().getLocation();
        if (!new EntityPortalEnterEvent(event.getEntity(), location, portalType(material)).callEvent()) return;

        entity.setAsInsidePortal(portals.computeIfAbsent(material, key -> new LinkedPortal(key, vanilla)), ((CraftBlock) event.getBlock()).getPosition());
    }

    /**
     * The linked world of the portal type in {@link PortalConfig}.
     *
     * @param sourceLevel the level of the portal
     * @param material    the material of the portal block
     * @return the linked level, or {@code null} if the world is not linked
     */
    private ServerLevel targetLevel(ServerLevel sourceLevel, Material material) {
        World sourceWorld = sourceLevel.getWorld();
        PortalConfig portalConfig = plugin.get(PortalConfig.class);
        Optional<World> targetWorld = switch (material) {
            case NETHER_PORTAL -> portalConfig.getWorldFromNetherPortal(sourceWorld);
            case END_PORTAL -> portalConfig.getWorldFromEndPortal(sourceWorld);
            default -> Optional.empty();
        };
        return targetWorld.map(world -> ((CraftWorld) world).getHandle()).orElse(null);
    }

    /**
     * The {@link PortalType} of the portal block for {@link EntityPortalEnterEvent}.
     *
     * @param material the material of the portal block
     * @return the portal type
     */
    private static PortalType portalType(Material material) {
        return switch (material) {
            case NETHER_PORTAL -> PortalType.NETHER;
            case END_PORTAL -> PortalType.ENDER;
            default -> PortalType.CUSTOM;
        };
    }

    /**
     * The portal of a link. Everything except the destination is done by the server:
     * {@link Portal#getPortalTransitionTime} and {@link Portal#getLocalTransition} come from the vanilla portal
     * and {@link Portal#portalAsync} only has to build the transition.
     */
    private final class LinkedPortal implements Portal {
        private final Material material;
        private final Portal vanilla;

        private LinkedPortal(Material material, Portal vanilla) {
            this.material = material;
            this.vanilla = vanilla;
        }

        /**
         * {@link NetherPortalBlock#getPortalTransitionTime}, the delay of the game rule for the players.
         */
        @Override
        public int getPortalTransitionTime(ServerLevel level, Entity entity) {
            return vanilla.getPortalTransitionTime(level, entity);
        }

        /**
         * {@link Portal#getLocalTransition}, the confusion effect of the nether portal.
         */
        @Override
        public Portal.Transition getLocalTransition() {
            return vanilla.getLocalTransition();
        }

        @Override
        public TeleportTransition getPortalDestination(ServerLevel sourceLevel, Entity entity, BlockPos portalPos) {
            // Folia only uses the region-threaded portalAsync, this is here for the interface
            portalAsync(sourceLevel, entity, portalPos);
            return null;
        }

        /**
         * The hook the server uses for its own portals, {@link NetherPortalBlock#portalAsync} and
         * {@link net.minecraft.world.level.block.EndPortalBlock#portalAsync}. A linked world that is the
         * destination of the server itself is left to {@link Entity#netherPortalLogicAsync} or
         * {@link Entity#endPortalLogicAsync}, which includes the passenger tree and the chunk tickets of
         * {@link Entity#portalToAsync}.
         */
        @Override
        public boolean portalAsync(ServerLevel sourceLevel, Entity entity, BlockPos portalPos) {
            ServerLevel destination = targetLevel(sourceLevel, material);
            if (destination == null) return false;
            debug.debug("Portal " + material + ": " + entity.getUUID() + " (" + sourceLevel.getWorld().getName() + " -> " + destination.getWorld().getName() + ") at " + portalPos);

            if (destination == defaultDestination(sourceLevel)) {
                debug.debug("Default destination, using the portal logic of the server");
                return switch (material) {
                    case NETHER_PORTAL -> entity.netherPortalLogicAsync(portalPos);
                    case END_PORTAL -> entity.endPortalLogicAsync(portalPos);
                    default -> false;
                };
            }
            return switch (material) {
                case NETHER_PORTAL -> teleportNether(sourceLevel, destination, entity, portalPos);
                case END_PORTAL -> teleportEnd(sourceLevel, destination, entity);
                default -> false;
            };
        }

        /**
         * The destination the server would use on its own, {@link Entity#netherPortalLogicAsync} and
         * {@link Entity#endPortalLogicAsync}.
         *
         * @param sourceLevel the level of the portal
         * @return the level of {@link Level#OVERWORLD}, {@link Level#NETHER} or {@link Level#END}
         */
        private ServerLevel defaultDestination(ServerLevel sourceLevel) {
            ResourceKey<Level> targetKey = switch (material) {
                case NETHER_PORTAL -> sourceLevel.getTypeKey() == LevelStem.NETHER ? Level.OVERWORLD : Level.NETHER;
                case END_PORTAL -> sourceLevel.getTypeKey() == LevelStem.END ? Level.OVERWORLD : Level.END;
                default -> null;
            };
            return targetKey == null ? null : sourceLevel.getServer().getLevel(targetKey);
        }

        /**
         * {@link NetherPortalBlock#getPortalDestination}: the exit position is scaled with
         * {@link DimensionType#getTeleportationScale} and clamped with {@link WorldBorder#clampToBounds}, the
         * source portal comes from {@link NetherPortalBlock#getDimensionTransitionFromExit} with
         * {@link BlockUtil#getLargestRectangleAround} and {@link Entity#getRelativePortalPosition}, and the
         * portal is searched with {@link NetherPortalBlock#findPortalAround} or created with
         * {@link net.minecraft.world.level.portal.PortalForcer#createPortal(BlockPos, Direction.Axis, Entity, int)}
         * like {@link Entity#findOrCreatePortalAsync}, after loading the chunks with
         * {@link ServerLevel#moonrise$loadChunksAsync(BlockPos, int, ChunkStatus, ca.spottedleaf.concurrentutil.util.Priority, java.util.function.Consumer)}.
         */
        private boolean teleportNether(ServerLevel sourceLevel, ServerLevel destination, Entity entity, BlockPos portalPos) {
            // exit position
            WorldBorder border = destination.getWorldBorder();
            double scale = DimensionType.getTeleportationScale(sourceLevel.dimensionType(), destination.dimensionType());
            BlockPos exitPos = border.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

            // source portal
            BlockState sourcePortal = sourceLevel.getBlockStateIfLoaded(portalPos);
            Direction.Axis sourceAxis = sourcePortal == null ? Direction.Axis.X : sourcePortal.getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
            BlockUtil.FoundRectangle sourceArea = sourcePortal == null || !sourcePortal.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) ? null
                    : BlockUtil.getLargestRectangleAround(portalPos, sourceAxis, 21, Direction.Axis.Y, 21,
                    pos -> sourceLevel.getBlockStateFromEmptyChunkIfLoaded(pos) == sourcePortal);
            Vec3 relative = sourceArea == null ? new Vec3(0.5, 0.0, 0.0) : entity.getRelativePortalPosition(sourceAxis, sourceArea);

            // search and creation radius, with the Paper dimension scaling
            int searchRadius = destination.paperConfig().environment.portalSearchVanillaDimensionScaling && destination.getTypeKey() == LevelStem.NETHER
                    ? (int) (destination.paperConfig().environment.portalSearchRadius / destination.dimensionType().coordinateScale())
                    : destination.paperConfig().environment.portalSearchRadius;
            int createRadius = destination.paperConfig().environment.portalCreateRadius;
            debug.debug("Nether exit " + exitPos + " (search " + searchRadius + ", create " + createRadius + ")");

            // chunk loading, then the search
            destination.moonrise$loadChunksAsync(exitPos, searchRadius + SEARCH_CHUNK_MARGIN, ChunkStatus.EMPTY, Priority.HIGH, chunks -> {
                BlockUtil.FoundRectangle exitArea = NetherPortalBlock.findPortalAround(destination, exitPos, border, searchRadius);
                if (exitArea != null) {
                    debug.debug("Found portal " + exitArea.minCorner);
                    apply(entity, netherTransition(destination, exitArea, sourceAxis, relative, entity));
                    return;
                }

                // chunk loading, then the double check and the creation
                destination.moonrise$loadChunksAsync(exitPos, createRadius + SEARCH_CHUNK_MARGIN, Priority.HIGH, chunks2 -> {
                    BlockUtil.FoundRectangle createdArea = NetherPortalBlock.findPortalAround(destination, exitPos, border, searchRadius);
                    if (createdArea == null) {
                        createdArea = destination.getPortalForcer().createPortal(exitPos, sourceAxis, entity, createRadius).orElse(null);
                    }
                    if (createdArea == null) {
                        debug.debug("Unable to create a portal at " + exitPos);
                        return;
                    }
                    debug.debug("Created portal " + createdArea.minCorner);
                    apply(entity, netherTransition(destination, createdArea, sourceAxis, relative, entity));
                });
            });
            return true;
        }

        /**
         * {@link NetherPortalBlock#createDimensionTransition}, with the sound and the chunk ticket of the
         * server.
         */
        private TeleportTransition netherTransition(ServerLevel destination, BlockUtil.FoundRectangle area, Direction.Axis sourceAxis, Vec3 relative, Entity entity) {
            return NetherPortalBlock.createDimensionTransition(destination, area, sourceAxis, relative, entity,
                    TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
        }

        /**
         * {@link net.minecraft.world.level.block.EndPortalBlock#getPortalDestination}: the respawn block is
         * resolved with {@link ServerPlayer#findRespawnPositionAndUseSpawnBlock}, the platform is built with
         * {@link EndPlatformFeature#createEndPlatform} and the heightmap spawn is read like
         * {@link Entity#adjustSpawnLocation} does.
         */
        private boolean teleportEnd(ServerLevel sourceLevel, ServerLevel destination, Entity entity) {
            if (sourceLevel.getTypeKey() == LevelStem.END && entity instanceof ServerPlayer player && player.getBukkitEntity().getRespawnLocation() != null) {
                TeleportTransition respawn = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING, PlayerRespawnEvent.RespawnReason.END_PORTAL);
                if (respawn != null) {
                    apply(entity, respawn);
                    return true;
                }
            }

            // the spawn of the linked world, the server uses the respawn data of the level instead
            BlockPos spawnPos = CraftLocation.toBlockPos(spawnLocation(destination));
            debug.debug("End spawn " + spawnPos);
            if (destination.getTypeKey() == LevelStem.END) {
                // platform below the spawn
                destination.moonrise$loadChunksAsync(spawnPos, 16, Priority.HIGH, chunks -> {
                    EndPlatformFeature.createEndPlatform(destination, spawnPos.below(), true, entity);
                    apply(entity, endTransition(destination, Vec3.atBottomCenterOf(spawnPos.below()), Direction.WEST.toYRot(), 0.0F, Relative.union(Relative.DELTA, Set.of(Relative.X_ROT))));
                });
            } else {
                // heightmap spawn
                destination.moonrise$loadChunksAsync(spawnPos, 0, Priority.HIGH, chunks -> {
                    BlockPos adjustedSpawn = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
                    apply(entity, endTransition(destination, Vec3.atBottomCenterOf(adjustedSpawn), 0.0F, 0.0F, Relative.union(Relative.DELTA, Relative.ROTATION)));
                });
            }
            return true;
        }

        /**
         * The transition of the end portal, with the relatives and the cause of
         * {@link net.minecraft.world.level.block.EndPortalBlock#getPortalDestination}.
         */
        private TeleportTransition endTransition(ServerLevel destination, Vec3 pos, float yaw, float pitch, Set<Relative> relatives) {
            return new TeleportTransition(
                    destination, pos, Vec3.ZERO, yaw, pitch,
                    false, false,
                    relatives,
                    TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET),
                    PlayerTeleportEvent.TeleportCause.END_PORTAL,
                    TeleportTransition.PassengerTeleportationMode.POSITION_RIDER
            );
        }

        /**
         * The teleport of {@link Entity#findOrCreatePortalAsync}, with the flags and the post transition of
         * {@link Entity#portalToAsync}, but {@link Entity#teleportAsync} has to run on the region of the entity.
         */
        private void apply(Entity entity, TeleportTransition transition) {
            debug.debug("Teleport " + entity.getUUID() + " to " + transition.newLevel().getWorld().getName() + " " + transition.position());
            entity.getBukkitEntity().getScheduler().execute(plugin, () -> entity.teleportAsync(
                    transition,
                    Entity.TELEPORT_FLAG_LOAD_CHUNK | Entity.TELEPORT_FLAG_TELEPORT_PASSENGERS,
                    teleported -> {
                        TeleportTransition.PostTeleportTransition post = transition.postTeleportTransition();
                        if (post != null) {
                            post.onTransition(teleported);
                        }
                    }
            ), null, 1L);
        }

        /**
         * The spawn of the linked world, the plugin spawn config first, then {@link World#getSpawnLocation}.
         *
         * @param level the level of the linked world
         * @return the spawn of the linked world
         */
        private Location spawnLocation(ServerLevel level) {
            World world = level.getWorld();
            return plugin.get(WorldSpawnConfig.class).getSpawn(world).map(position -> position.toLocation(world)).orElseGet(world::getSpawnLocation);
        }
    }
}
