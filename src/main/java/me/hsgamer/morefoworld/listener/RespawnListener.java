package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.RespawnConfig;
import me.hsgamer.morefoworld.config.WorldSpawnConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

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
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = player.getLocation();
        debug.debug("Death: " + player.getName() + " at " + location);

        World world = location.getWorld();
        Optional<World> optionalRespawnWorld = plugin.get(RespawnConfig.class).getRespawnWorld(world);
        if (optionalRespawnWorld.isEmpty()) return;
        World respawnWorld = optionalRespawnWorld.get();
        Location respawnLocation = plugin.get(WorldSpawnConfig.class).getSpawn(respawnWorld).map(position -> position.toLocation(respawnWorld)).orElseGet(respawnWorld::getSpawnLocation);
        debug.debug("Set Respawn to " + respawnWorld);

        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (player.isDead()) return;
            task.cancel();
            player.teleportAsync(respawnLocation);
            debug.debug("Respawned: " + player.getName() + " at " + respawnLocation);
        }, null, 1, 1);
        debug.debug("Scheduled Respawn");
    }
}
