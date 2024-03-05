package me.hsgamer.morefoworld;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.command.CommandComponent;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.morefoworld.command.MainCommand;
import me.hsgamer.morefoworld.config.MainConfig;
import me.hsgamer.morefoworld.config.PortalConfig;
import me.hsgamer.morefoworld.config.RespawnConfig;
import me.hsgamer.morefoworld.config.SpawnConfig;
import me.hsgamer.morefoworld.listener.PortalListener;
import me.hsgamer.morefoworld.listener.RespawnListener;
import me.hsgamer.morefoworld.listener.SpawnListener;
import org.bukkit.WorldCreator;

import java.util.List;

public final class MoreFoWorld extends BasePlugin {
    @Override
    protected List<Object> getComponents() {
        return List.of(
                ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this)),
                ConfigGenerator.newInstance(PortalConfig.class, new BukkitConfig(this, "portals.yml")),
                ConfigGenerator.newInstance(RespawnConfig.class, new BukkitConfig(this, "respawn.yml")),
                ConfigGenerator.newInstance(SpawnConfig.class, new BukkitConfig(this, "spawn.yml")),
                new DebugComponent(this),
                new PortalListener(this),
                new RespawnListener(this),
                new SpawnListener(this),
                new CommandComponent(this, () -> List.of(new MainCommand(this)))
        );
    }

    @Override
    public void load() {
        MessageUtils.setPrefix("&8[&6MoreFoWorld&8] &r");
    }

    @Override
    public void enable() {
        for (WorldSetting worldSetting : get(MainConfig.class).getWorldSettings()) {
            WorldCreator worldCreator = worldSetting.toWorldCreator();
            WorldUtil.FeedbackWorld feedbackWorld = WorldUtil.addWorld(worldCreator);
            if (feedbackWorld.feedback == WorldUtil.Feedback.SUCCESS) {
                getLogger().info("World " + worldSetting.getName() + " is added");
            } else {
                getLogger().warning("World " + worldSetting.getName() + " is not added: " + feedbackWorld.feedback);
            }
        }
    }
}
