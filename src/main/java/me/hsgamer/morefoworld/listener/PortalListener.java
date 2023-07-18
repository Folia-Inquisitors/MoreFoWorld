package me.hsgamer.morefoworld.listener;

import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.Optional;

public class PortalListener implements Listener {
    private final MoreFoWorld plugin;

    public PortalListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        plugin.debug("Portal Cause: " + event.getCause());
        plugin.debug("From: " + from);
        plugin.debug("To: " + to);
        Optional<World> worldOptional = switch (event.getCause()) {
            case NETHER_PORTAL -> plugin.getPortalConfig().getWorldFromNetherPortal(from.getWorld());
            case END_PORTAL -> plugin.getPortalConfig().getWorldFromEndPortal(from.getWorld());
            default -> Optional.empty();
        };
        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);
            event.setTo(clone);
            plugin.debug("Set Portal to " + clone);
        });
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        plugin.debug("Entity Portal: " + event.getPortalType());
        plugin.debug("From: " + from);
        plugin.debug("To: " + to);
        if (to == null) {
            return;
        }
        Optional<World> worldOptional = switch (event.getPortalType()) {
            case NETHER -> plugin.getPortalConfig().getWorldFromNetherPortal(from.getWorld());
            case ENDER -> plugin.getPortalConfig().getWorldFromEndPortal(from.getWorld());
            default -> Optional.empty();
        };
        worldOptional.ifPresent(world -> {
            Location clone = to.clone();
            clone.setWorld(world);
            event.setTo(clone);
            plugin.debug("Set Portal to " + clone);
        });
    }
}
