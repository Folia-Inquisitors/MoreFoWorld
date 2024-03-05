package me.hsgamer.morefoworld;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import me.hsgamer.morefoworld.config.MainConfig;

public class DebugComponent {
    private final BasePlugin basePlugin;

    public DebugComponent(BasePlugin basePlugin) {
        this.basePlugin = basePlugin;
    }

    public void debug(String message) {
        if (basePlugin.get(MainConfig.class).isDebug()) {
            basePlugin.getLogger().info(message);
        }
    }
}
