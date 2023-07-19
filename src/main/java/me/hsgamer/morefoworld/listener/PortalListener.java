package me.hsgamer.morefoworld.listener;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class PortalListener implements Listener {
    private final MoreFoWorld plugin;

    public PortalListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortalReady(EntityPortalReadyEvent event) {
        if (event.getPortalType() != PortalType.NETHER) return;
        plugin.debug("Portal Ready: " + event.getEntity().getWorld());
        plugin.debug("Portal Type: " + event.getPortalType());
        plugin.getPortalConfig().getWorldFromNetherPortal(event.getEntity().getWorld()).ifPresent(world -> {
            event.setTargetWorld(world);
            plugin.debug("Set Portal to " + world);
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
            entity.teleportAsync(location).thenRun(() -> plugin.debug("Teleported to " + location));
        });
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        plugin.debug("Portal Cause: " + event.getCause());
        plugin.debug("From: " + from);
        plugin.debug("To: " + to);
        Optional<World> worldOptional = plugin.getPortalConfig().getWorldFromEndPortal(from.getWorld());

        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);

            event.setCancelled(true);
            if (world.getEnvironment() == World.Environment.THE_END) {
                teleportToEnd(event.getPlayer(), clone);
                plugin.debug("Teleport to " + clone);
            } else {
                event.getPlayer().teleportAsync(clone).thenRun(() -> plugin.debug("Teleported to " + clone));
            }
        });
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getPortalType() != PortalType.ENDER) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        plugin.debug("Entity Portal: " + event.getPortalType());
        plugin.debug("From: " + from);
        plugin.debug("To: " + to);
        if (to == null) {
            return;
        }

        Optional<World> worldOptional = plugin.getPortalConfig().getWorldFromEndPortal(from.getWorld());

        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);

            event.setCancelled(true);
            if (world.getEnvironment() == World.Environment.THE_END) {
                teleportToEnd(event.getEntity(), clone);
                plugin.debug("Teleport to " + clone);
            } else {
                event.getEntity().teleportAsync(clone).thenRun(() -> plugin.debug("Teleported to " + clone));
            }
        });
    }
}
