package me.hsgamer.morefoworld.initializer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public interface WorldInitializer {
    FeedbackWorld addWorld(WorldCreator creator);

    CompletableFuture<Feedback> unloadWorld(Plugin plugin, World world, boolean save);

    default void applyWorldSpawn(Location location) {
        location.getWorld().setSpawnLocation(location);
    }

    enum Feedback {
        WORLD_ALREADY_EXISTS,
        ERROR,
        SUCCESS,
        WORLD_NOT_FOUND,
        CANNOT_UNLOAD_OVERWORLD,
        PLAYERS_ONLINE,
        UNLOAD_CANCELLED;

        public FeedbackWorld toFeedbackWorld(World world) {
            return new FeedbackWorld(world, this, null);
        }

        public FeedbackWorld toFeedbackWorld() {
            return new FeedbackWorld(this, null);
        }

        public FeedbackWorld toFeedbackWorld(Throwable throwable) {
            return new FeedbackWorld(this, throwable);
        }
    }

    record FeedbackWorld(World world, Feedback feedback, Throwable throwable) {
        public FeedbackWorld(Feedback feedback, Throwable throwable) {
            this(null, feedback, throwable);
        }
    }
}
