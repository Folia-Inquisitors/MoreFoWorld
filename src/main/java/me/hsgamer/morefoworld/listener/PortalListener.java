package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.PortalConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PortalListener implements ListenerComponent {
    private final ConcurrentHashMap<UUID, Material> portalTeleportCache = new ConcurrentHashMap<>();
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

    @EventHandler
    public void onPortalReady(EntityPortalReadyEvent event) {
        if (event.getPortalType() != PortalType.NETHER) return;
        debug.debug("Portal Ready: " + event.getEntity().getWorld());
        debug.debug("Portal Type: " + event.getPortalType());
        plugin.get(PortalConfig.class).getWorldFromNetherPortal(event.getEntity().getWorld()).ifPresent(world -> {
            event.setTargetWorld(world);
            debug.debug("Set Portal to " + world);
        });
    }

    private CompletableFuture<Void> constructEndPlatform(Location location) {
        return CompletableFuture.runAsync(() -> {
            Block block = location.getBlock();
            for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                    Block platformBlock = block.getWorld().getBlockAt(x, block.getY() - 1, z);
                    if (platformBlock.getType() != Material.OBSIDIAN) {
                        platformBlock.setType(Material.OBSIDIAN);
                    }
                    for (int yMod = 1; yMod <= 3; yMod++) {
                        Block b = platformBlock.getRelative(BlockFace.UP, yMod);
                        if (b.getType() != Material.AIR) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }, runnable -> Bukkit.getRegionScheduler().execute(plugin, location, runnable));
    }

    private CompletableFuture<Void> constructNetherPortal(Location location) {
        return CompletableFuture.runAsync(() -> {
            // TODO: Construct the nether portal
        }, runnable -> Bukkit.getRegionScheduler().execute(plugin, location, runnable));
    }

    private CompletableFuture<Boolean> teleport(Entity entity, Location location, boolean runInScheduler) {
        if (runInScheduler) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            entity.getScheduler().execute(plugin, () -> entity.teleportAsync(location).thenAccept(future::complete), null, 1L);
            return future;
        } else {
            return entity.teleportAsync(location);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        debug.debug("Portal Cause: " + event.getCause());
        debug.debug("From: " + from);
        debug.debug("To: " + to);
        Optional<World> worldOptional = plugin.get(PortalConfig.class).getWorldFromEndPortal(from.getWorld());

        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);
        });
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getPortalType() != PortalType.ENDER) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        debug.debug("Entity Portal: " + event.getPortalType());
        debug.debug("From: " + from);
        debug.debug("To: " + to);
        if (to == null) {
            return;
        }

        Optional<World> worldOptional = plugin.get(PortalConfig.class).getWorldFromEndPortal(from.getWorld());

        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);

            event.setCancelled(true);
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            if (world.getEnvironment() == World.Environment.THE_END) {
                future = constructEndPlatform(clone);
            }
            future
                    .thenCompose(aVoid -> teleport(event.getEntity(), clone, true))
                    .thenRun(() -> debug.debug("Teleported to " + clone));
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInsidePortal(final EntityInsideBlockEvent event) {
        Block block = event.getBlock();
        Entity entity = event.getEntity();
        Material blockTypeInside = block.getType();
        Location from = entity.getLocation();

        if (portalTeleportCache.containsKey(entity.getUniqueId())) {
            debug.debug("The entity is being teleported");
            return;
        }

        Optional<World> toWorldOptional = switch (blockTypeInside) {
            case NETHER_PORTAL -> plugin.get(PortalConfig.class).getWorldFromNetherPortal(from.getWorld());
            case END_PORTAL -> plugin.get(PortalConfig.class).getWorldFromEndPortal(from.getWorld());
            default -> Optional.empty();
        };
        if (toWorldOptional.isEmpty()) return;
        World toWorld = toWorldOptional.get();

        portalTeleportCache.put(entity.getUniqueId(), blockTypeInside);

        event.setCancelled(true);

        entity.getScheduler().execute(plugin, () -> {
            Block currentBlock = entity.getLocation().getBlock();
            if (currentBlock.getType() != blockTypeInside) {
                debug.debug("The entity is not in the portal");
                portalTeleportCache.remove(entity.getUniqueId());
                return;
            }

            World.Environment fromEnvironment = from.getWorld().getEnvironment();
            World.Environment toEnvironment = toWorld.getEnvironment();
            Location to;
            if (toEnvironment == World.Environment.THE_END) {
                to = toWorld.getSpawnLocation();
            } else if (fromEnvironment == World.Environment.NORMAL && toEnvironment == World.Environment.NETHER) {
                to = from.clone();
                to.setWorld(toWorld);
                to.setX(to.getX() / 8);
                to.setZ(to.getZ() / 8);
            } else if (fromEnvironment == World.Environment.NETHER && toEnvironment == World.Environment.NORMAL) {
                to = from.clone();
                to.setWorld(toWorld);
                to.setX(to.getX() * 8);
                to.setZ(to.getZ() * 8);
            } else {
                to = from.clone();
                to.setWorld(toWorld);
            }

            switch (toEnvironment) {
                case THE_END -> {
                    constructEndPlatform(to)
                            .thenCompose(aVoid -> teleport(entity, to, false))
                            .thenRun(() -> debug.debug("Teleported to " + to));
                }
                case NETHER -> {
                    constructNetherPortal(to)
                            .thenCompose(aVoid -> teleport(entity, to, false))
                            .thenRun(() -> debug.debug("Teleported to " + to));
                }
                default -> teleport(entity, to, false).thenRun(() -> debug.debug("Teleported to " + to));
            }

            portalTeleportCache.remove(entity.getUniqueId());
        }, null, 1L);
    }
}
