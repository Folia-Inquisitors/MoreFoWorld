package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.PortalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PortalListener implements ListenerComponent {
    private static final Class<?> PORTAL_TYPE_CLASS;
    private static final Object NETHER_PORTAL_TYPE;
    private static final Object END_PORTAL_TYPE;
    private static final Method PRE_PORTAL_LOGIC_METHOD;
    private static final Method PORTAL_TO_ASYNC_METHOD;

    static {
        try {
            PORTAL_TYPE_CLASS = Class.forName("net.minecraft.world.entity.Entity$PortalType");

            PRE_PORTAL_LOGIC_METHOD = net.minecraft.world.entity.Entity.class.getDeclaredMethod("prePortalLogic", ServerLevel.class, ServerLevel.class, PORTAL_TYPE_CLASS);
            PRE_PORTAL_LOGIC_METHOD.setAccessible(true);

            PORTAL_TO_ASYNC_METHOD = net.minecraft.world.entity.Entity.class.getDeclaredMethod("portalToAsync", ServerLevel.class, BlockPos.class, boolean.class, PORTAL_TYPE_CLASS, Consumer.class);
            PORTAL_TO_ASYNC_METHOD.setAccessible(true);

            NETHER_PORTAL_TYPE = PORTAL_TYPE_CLASS.getField("NETHER").get(null);
            END_PORTAL_TYPE = PORTAL_TYPE_CLASS.getField("END").get(null);
        } catch (NoSuchMethodException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

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

    @EventHandler(ignoreCancelled = true)
    public void onEntityInsidePortal(final EntityInsideBlockEvent event) {
        Block block = event.getBlock();
        Entity entity = event.getEntity();
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        Material blockTypeInside = block.getType();
        Location from = entity.getLocation();

        Object portalType = switch (blockTypeInside) {
            case NETHER_PORTAL -> NETHER_PORTAL_TYPE;
            case END_PORTAL -> END_PORTAL_TYPE;
            default -> null;
        };
        debug.debug("Portal Type: " + portalType);
        if (portalType == null) return;

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
        ServerLevel toServerLevel = ((CraftWorld) toWorld).getHandle();

        World fromWorld = from.getWorld();
        if (fromWorld == toWorld) return;
        ServerLevel fromServerLevel = ((CraftWorld) fromWorld).getHandle();

        portalTeleportCache.put(entity.getUniqueId(), blockTypeInside);

        event.setCancelled(true);

        entity.getScheduler().execute(plugin, () -> {
            Block currentBlock = entity.getLocation().getBlock();
            if (currentBlock.getType() != blockTypeInside) {
                debug.debug("The entity is not in the portal");
                portalTeleportCache.remove(entity.getUniqueId());
                return;
            }

            try {
                PRE_PORTAL_LOGIC_METHOD.invoke(nmsEntity, fromServerLevel, toServerLevel, portalType);
                boolean teleportSuccess = (boolean) PORTAL_TO_ASYNC_METHOD.invoke(nmsEntity, toServerLevel, new BlockPos(block.getX(), block.getY(), block.getZ()), true, portalType, (Consumer<net.minecraft.world.entity.Entity>) e -> {
                    portalTeleportCache.remove(entity.getUniqueId());
                    debug.debug("Portal Teleported to " + toWorld);
                });
                debug.debug("Teleport Success: " + teleportSuccess);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to portal teleport entity", e);
                portalTeleportCache.remove(entity.getUniqueId());
            }
        }, null, 1L);
    }
}
