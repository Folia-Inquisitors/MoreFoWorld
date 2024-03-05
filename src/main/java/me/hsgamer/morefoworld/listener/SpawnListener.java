package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.config.SpawnConfig;
import org.bukkit.event.EventHandler;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class SpawnListener extends ListenerComponent {
    public SpawnListener(BasePlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        SpawnConfig spawnConfig = plugin.get(SpawnConfig.class);

        if (!spawnConfig.isEnabled()) return;
        if (spawnConfig.isFirstJoin() && !event.getPlayer().hasPlayedBefore()) return;

        event.setSpawnLocation(spawnConfig.getPosition().toLocation());
    }
}
