package me.hsgamer.morefoworld.initializer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public final class CanvasWorldInitializer implements WorldInitializer {
    public static boolean isAvailable() {
        try {
            Class.forName("io.canvasmc.canvas.region.WorldRegionizer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public FeedbackWorld addWorld(WorldCreator creator) {
        String name = creator.name();
        if (Bukkit.getWorld(name) != null || Bukkit.getWorld(creator.key()) != null) {
            return Feedback.WORLD_ALREADY_EXISTS.toFeedbackWorld();
        }
        try {
            World world = Bukkit.createWorld(creator);
            if (world != null) {
                return Feedback.SUCCESS.toFeedbackWorld(world);
            }
            return Feedback.ERROR.toFeedbackWorld(new RuntimeException("World creation returned null"));
        } catch (Throwable throwable) {
            return Feedback.ERROR.toFeedbackWorld(throwable);
        }
    }

    @Override
    public CompletableFuture<Feedback> unloadWorld(Plugin plugin, World world, boolean save) {
        CompletableFuture<Feedback> future = new CompletableFuture<>();
        Bukkit.getServer().unloadWorldAsync(world, save, result -> future.complete(switch (result) {
            case SUCCESS -> Feedback.SUCCESS;
            case FAIL_PLAYERS_JOINING, FAIL_PLAYERS_PRESENT -> Feedback.PLAYERS_ONLINE;
            case FAIL_ALREADY_UNLOADING -> Feedback.WORLD_NOT_FOUND;
            case FAIL_IS_OVERWORLD -> Feedback.CANNOT_UNLOAD_OVERWORLD;
            case FAIL_UNLOAD_EVENT -> Feedback.UNLOAD_CANCELLED;
            case FAIL_IS_SHUTDOWN, FAIL_UNKNOWN -> Feedback.ERROR;
        }));
        return future;
    }
}
