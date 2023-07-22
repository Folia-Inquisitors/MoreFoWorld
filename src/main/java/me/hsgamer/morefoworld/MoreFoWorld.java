package me.hsgamer.morefoworld;

import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.morefoworld.command.MainCommand;
import me.hsgamer.morefoworld.config.MainConfig;
import me.hsgamer.morefoworld.config.PortalConfig;
import me.hsgamer.morefoworld.config.RespawnConfig;
import me.hsgamer.morefoworld.listener.PortalListener;
import me.hsgamer.morefoworld.listener.RespawnListener;
import org.bukkit.WorldCreator;

public final class MoreFoWorld extends BasePlugin {
    private final MainConfig mainConfig = ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this));
    private final PortalConfig portalConfig = ConfigGenerator.newInstance(PortalConfig.class, new BukkitConfig(this, "portals.yml"));
    private final RespawnConfig respawnConfig = ConfigGenerator.newInstance(RespawnConfig.class, new BukkitConfig(this, "respawn.yml"));

    @Override
    public void load() {
        MessageUtils.setPrefix("&8[&6MoreFoWorld&8] &r");
    }

    @Override
    public void enable() {
        for (WorldSetting worldSetting : mainConfig.getWorldSettings()) {
            WorldCreator worldCreator = worldSetting.toWorldCreator();
            WorldUtil.FeedbackWorld feedbackWorld = WorldUtil.addWorld(worldCreator);
            if (feedbackWorld.feedback == WorldUtil.Feedback.SUCCESS) {
                getLogger().info("World " + worldSetting.getName() + " is added");
            } else {
                getLogger().warning("World " + worldSetting.getName() + " is not added: " + feedbackWorld.feedback);
            }
        }

        registerCommand(new MainCommand(this));
        registerListener(new PortalListener(this));
        registerListener(new RespawnListener(this));
    }

    public PortalConfig getPortalConfig() {
        return portalConfig;
    }

    public RespawnConfig getRespawnConfig() {
        return respawnConfig;
    }

    public void debug(String message) {
        if (mainConfig.isDebug()) {
            getLogger().info(message);
        }
    }
}
