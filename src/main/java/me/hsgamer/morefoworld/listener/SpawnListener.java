package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.config.SpawnConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpawnListener implements ListenerComponent {
    private final BasePlugin plugin;

    public SpawnListener(BasePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BasePlugin getPlugin() {
        return plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        SpawnConfig spawnConfig = plugin.get(SpawnConfig.class);
        Player player = event.getPlayer();

        if (!spawnConfig.isEnabled()) return;
        if (spawnConfig.isFirstJoin() && player.hasPlayedBefore()) return;

        Location location = spawnConfig.getPosition().toLocation();
        Bukkit.getRegionScheduler().execute(plugin, location, () -> player.teleportAsync(location));
    }
}
