package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
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

import java.util.Optional;

public class PortalListener extends ListenerComponent {
    private final DebugComponent debug = plugin.get(DebugComponent.class);

    public PortalListener(BasePlugin plugin) {
        super(plugin);
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

    private void teleportToEnd(Entity entity, Location location) {
        Bukkit.getRegionScheduler().execute(plugin, location, () -> {
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
            entity.teleportAsync(location).thenRun(() -> debug.debug("Teleported to " + location));
        });
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

            event.setCancelled(true);
            if (world.getEnvironment() == World.Environment.THE_END) {
                teleportToEnd(event.getPlayer(), clone);
                debug.debug("Teleport to " + clone);
            } else {
                event.getPlayer().teleportAsync(clone).thenRun(() -> debug.debug("Teleported to " + clone));
            }
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
            if (world.getEnvironment() == World.Environment.THE_END) {
                teleportToEnd(event.getEntity(), clone);
                debug.debug("Teleport to " + clone);
            } else {
                event.getEntity().teleportAsync(clone).thenRun(() -> debug.debug("Teleported to " + clone));
            }
        });
    }
}
