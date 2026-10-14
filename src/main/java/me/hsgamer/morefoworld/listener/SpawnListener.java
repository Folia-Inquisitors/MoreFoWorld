package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.config.SpawnConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Teleports the player to the spawn of {@link SpawnConfig} when they join the server.
 * <p>
 * The server places a joining player at the spawn of the level with
 * {@link net.minecraft.server.level.ServerPlayer#fudgeSpawnLocation}, this listener replaces that spawn with
 * the configured one. {@link org.bukkit.entity.Entity#teleportAsync(org.bukkit.Location)} can be called from
 * any thread, the region hop is done by
 * {@link org.bukkit.craftbukkit.entity.CraftEntity#teleportAsync(org.bukkit.Location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause, org.bukkit.event.player.PlayerTeleportEvent.TeleportFlag...)}.
 */
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

        player.teleportAsync(spawnConfig.getPosition().toLocation());
    }
}
