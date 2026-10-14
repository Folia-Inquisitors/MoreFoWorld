package me.hsgamer.morefoworld.listener;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.listener.ListenerComponent;
import me.hsgamer.morefoworld.DebugComponent;
import me.hsgamer.morefoworld.config.RespawnConfig;
import me.hsgamer.morefoworld.config.WorldSpawnConfig;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Optional;

/**
 * Respawns the player at the spawn of the linked world, see {@link RespawnConfig} and {@link WorldSpawnConfig}.
 * <p>
 * The player is teleported once the server respawned them, because Folia does not fire
 * {@link PlayerRespawnEvent} for a death: {@link ServerPlayer#respawn(java.util.function.Consumer, PlayerRespawnEvent.RespawnReason)}
 * computes the spawn with the static {@link ServerPlayer#findRespawnAndUseSpawnBlock}, and
 * {@link ServerPlayer#findRespawnPositionAndUseSpawnBlock}, the method that fires the event, is only called by
 * {@link net.minecraft.world.level.block.EndPortalBlock#getPortalDestination}.
 * <p>
 * shortcut: the polling of the respawn, replace it with
 * {@link PlayerRespawnEvent#setRespawnLocation(Location)} when Folia fires the event for a death.
 */
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

    /**
     * Sets the respawn location of the death with {@link RespawnConfig} and {@link WorldSpawnConfig}, and waits
     * for the respawn of the server with the
     * {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler} of the player before the teleport
     * ({@link org.bukkit.entity.Entity#teleportAsync(org.bukkit.Location)}).
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getRespawnLocation() != null) {
            debug.debug("Player has respawn location. Ignored!");
            return;
        }

        Location location = player.getLocation();
        debug.debug("Death: " + player.getName() + " at " + location);
        World world = location.getWorld();

        Location respawnLocation;
        Optional<World> optionalRespawnWorld = plugin.get(RespawnConfig.class).getRespawnWorld(world);
        if (optionalRespawnWorld.isPresent()) {
            World respawnWorld = optionalRespawnWorld.get();
            respawnLocation = plugin.get(WorldSpawnConfig.class).getSpawn(respawnWorld).map(position -> position.toLocation(respawnWorld)).orElseGet(respawnWorld::getSpawnLocation);
        } else {
            respawnLocation = plugin.get(WorldSpawnConfig.class).getSpawn(world).map(position -> position.toLocation(world)).orElse(null);
        }
        if (respawnLocation == null) {
            debug.debug("Respawn location is null");
            return;
        }
        debug.debug("Set Respawn to " + respawnLocation);

        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (player.isDead()) return;
            task.cancel();
            player.teleportAsync(respawnLocation);
            debug.debug("Respawned: " + player.getName() + " at " + respawnLocation);
        }, null, 1, 1);
        debug.debug("Scheduled Respawn");
    }
}
