package me.hsgamer.morefoworld;

import io.github.projectunified.craftcommand.paper.PaperCommandManager;
import io.github.projectunified.minelib.plugin.base.BasePlugin;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.morefoworld.config.*;
import me.hsgamer.morefoworld.config.object.Position;
import me.hsgamer.morefoworld.initializer.CanvasWorldInitializer;
import me.hsgamer.morefoworld.initializer.FoliaWorldInitializer;
import me.hsgamer.morefoworld.initializer.WorldInitializer;
import me.hsgamer.morefoworld.listener.PortalListener;
import me.hsgamer.morefoworld.listener.RespawnListener;
import me.hsgamer.morefoworld.listener.SpawnListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MoreFoWorld extends BasePlugin {
    @Override
    protected List<Object> getComponents() {
        return List.of(
                ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this)),
                ConfigGenerator.newInstance(PortalConfig.class, new BukkitConfig(this, "portals.yml")),
                ConfigGenerator.newInstance(RespawnConfig.class, new BukkitConfig(this, "respawn.yml")),
                ConfigGenerator.newInstance(SpawnConfig.class, new BukkitConfig(this, "spawn.yml")),
                ConfigGenerator.newInstance(WorldSpawnConfig.class, new BukkitConfig(this, "world-spawn.yml")),
                new DebugComponent(this),
                new PortalListener(this),
                new RespawnListener(this),
                new SpawnListener(this),
                new PaperCommandManager(this, (sender, exception) -> MessageUtils.sendMessage(sender.getSender(), "&c" + exception.getMessage())),
                CanvasWorldInitializer.isAvailable() ? new CanvasWorldInitializer() : new FoliaWorldInitializer()
        );
    }

    @Override
    public void load() {
        MessageUtils.setPrefix("&8[&6MoreFoWorld&8] &r");
        get(PaperCommandManager.class).register(new Commands(this));
    }

    @Override
    public void enable() {
        WorldInitializer initializer = get(WorldInitializer.class);
        for (WorldSetting worldSetting : get(MainConfig.class).getWorldSettings()) {
            WorldCreator worldCreator = worldSetting.toWorldCreator();
            WorldInitializer.FeedbackWorld feedbackWorld = initializer.addWorld(worldCreator);
            if (feedbackWorld.feedback() == WorldInitializer.Feedback.SUCCESS) {
                getLogger().info("World " + worldSetting.getName() + " is added");
            } else {
                getLogger().log(Level.WARNING, "World " + worldSetting.getName() + " is not added: " + feedbackWorld.feedback(), feedbackWorld.throwable());
            }
        }

        for (Map.Entry<String, Position> spawnEntry : get(WorldSpawnConfig.class).getSpawn().entrySet()) {
            World world = Bukkit.getWorld(spawnEntry.getKey());
            if (world == null) return;
            initializer.applyWorldSpawn(spawnEntry.getValue().toLocation(world));
        }
    }
}
