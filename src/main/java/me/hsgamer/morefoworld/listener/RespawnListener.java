package me.hsgamer.morefoworld.listener;

import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;

public class RespawnListener implements Listener {
    private final MoreFoWorld plugin;

    public RespawnListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.debug("Respawn: " + event.getPlayer().getName() + " in " + event.getRespawnLocation());
        plugin.debug("Reason: " + event.getRespawnReason());

        Optional.ofNullable(event.getPlayer().getLastDeathLocation())
                .map(Location::getWorld)
                .flatMap(plugin.getRespawnConfig()::getRespawnWorld)
                .ifPresent(world -> {
                    Location respawnLocation = event.getRespawnLocation();
                    respawnLocation.setWorld(world);
                    event.setRespawnLocation(respawnLocation);
                    plugin.debug("Set Respawn to " + world);
                });
    }
}
