package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.RespawnConfig;
import org.bukkit.Bukkit;
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
        Location respawnLocation = respawnWorld.getSpawnLocation();
        debug.debug("Set Respawn to " + respawnWorld);

        RespawnRunnable runnable = new RespawnRunnable(player, respawnLocation);
        player.getScheduler().runAtFixedRate(plugin, t -> runnable.run(), null, 0, 1);
        debug.debug("Scheduled Respawn");
    }

    private class RespawnRunnable implements Runnable {
        private final Player player;
        private final Location location;

        private RespawnRunnable(Player player, Location location) {
            this.player = player;
            this.location = location;
        }

        @Override
        public void run() {
            if (player.isDead()) return;
            Bukkit.getRegionScheduler().execute(plugin, location, () -> player.teleportAsync(location));
        }
    }
}
