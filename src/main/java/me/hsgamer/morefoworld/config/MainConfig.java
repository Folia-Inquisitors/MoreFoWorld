package me.hsgamer.morefoworld.config;

import me.hsgamer.hscore.config.annotation.Comment;
import me.hsgamer.hscore.config.annotation.ConfigPath;
import me.hsgamer.morefoworld.WorldSetting;
import me.hsgamer.morefoworld.config.converter.WorldSettingListConverter;

import java.util.ArrayList;
import java.util.List;

public interface MainConfig {
    @ConfigPath(value = "worlds", converter = WorldSettingListConverter.class)
    @Comment("The settings for each world")
    default List<WorldSetting> getWorldSettings() {
        ArrayList<WorldSetting> worldSettings = new ArrayList<>();
        worldSettings.add(new WorldSetting("world"));
        return worldSettings;
    }

    @ConfigPath("debug")
    default boolean isDebug() {
        return false;
    }
}
