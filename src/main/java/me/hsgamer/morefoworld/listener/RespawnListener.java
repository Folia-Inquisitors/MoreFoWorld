package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.RespawnConfig;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;

public class RespawnListener implements ListenerComponent {
    private final BasePlugin plugin;
    private DebugComponent debug;

    public RespawnListener(BasePlugin plugin) {
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
    public void onRespawn(PlayerRespawnEvent event) {
        debug.debug("Respawn: " + event.getPlayer().getName() + " in " + event.getRespawnLocation());
        debug.debug("Reason: " + event.getRespawnReason());

        Optional.ofNullable(event.getPlayer().getLastDeathLocation())
                .map(Location::getWorld)
                .flatMap(plugin.get(RespawnConfig.class)::getRespawnWorld)
                .ifPresent(world -> {
                    Location respawnLocation = event.getRespawnLocation();
                    respawnLocation.setWorld(world);
                    event.setRespawnLocation(respawnLocation);
                    debug.debug("Set Respawn to " + world);
                });
    }
}
