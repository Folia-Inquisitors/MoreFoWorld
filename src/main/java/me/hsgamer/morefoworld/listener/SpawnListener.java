package me.hsgamer.morefoworld.listener;

import me.hsgamer.morefoworld.MoreFoWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class SpawnListener implements Listener {
    private final MoreFoWorld plugin;

    public SpawnListener(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        if (!plugin.getSpawnConfig().isEnabled()) return;

        if (plugin.getSpawnConfig().isFirstJoin() && !event.getPlayer().hasPlayedBefore()) return;

        event.setSpawnLocation(plugin.getSpawnConfig().getPosition().toLocation());
    }
}
