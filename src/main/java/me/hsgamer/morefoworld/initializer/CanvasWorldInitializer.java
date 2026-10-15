package me.hsgamer.morefoworld.initializer;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The world initializer of Canvas, the API of Canvas is called through reflection, so that the plugin does not
 * need canvas-api on the classpath (canvas-api provides the same capability as folia-api and would replace it).
 */
public final class CanvasWorldInitializer implements WorldInitializer {
    /**
     * A class of Canvas to check whether the server is Canvas, {@code io.canvasmc.canvas.region.WorldRegionizer}.
     */
    private static final String CANVAS_MARKER = "io.canvasmc.canvas.region.WorldRegionizer";

    public static boolean isAvailable() {
        try {
            Class.forName(CANVAS_MARKER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Maps {@code io.canvasmc.canvas.WorldUnloadResult} to {@link Feedback} by its name.
     *
     * @param result the result of the unload
     * @return the feedback of the result
     */
    private static Feedback unloadFeedback(Object result) {
        String name = result instanceof Enum<?> unloadResult ? unloadResult.name() : "";
        return switch (name) {
            case "SUCCESS" -> Feedback.SUCCESS;
            case "FAIL_PLAYERS_JOINING", "FAIL_PLAYERS_PRESENT" -> Feedback.PLAYERS_ONLINE;
            case "FAIL_ALREADY_UNLOADING" -> Feedback.WORLD_NOT_FOUND;
            case "FAIL_IS_OVERWORLD" -> Feedback.CANNOT_UNLOAD_OVERWORLD;
            case "FAIL_UNLOAD_EVENT" -> Feedback.UNLOAD_CANCELLED;
            default -> Feedback.ERROR;
        };
    }

    @Override
    public CompletableFuture<FeedbackWorld> addWorld(WorldCreator creator) {
        String name = creator.name();
        if (Bukkit.getWorld(name) != null || Bukkit.getWorld(creator.key()) != null) {
            return CompletableFuture.completedFuture(Feedback.WORLD_ALREADY_EXISTS.toFeedbackWorld());
        }
        try {
            World world = Bukkit.createWorld(creator);
            if (world != null) {
                return CompletableFuture.completedFuture(Feedback.SUCCESS.toFeedbackWorld(world));
            }
            return CompletableFuture.completedFuture(Feedback.ERROR.toFeedbackWorld(new RuntimeException("World creation returned null")));
        } catch (Throwable throwable) {
            return CompletableFuture.completedFuture(Feedback.ERROR.toFeedbackWorld(throwable));
        }
    }

    /**
     * {@code Server#unloadWorldAsync(World, boolean, Consumer)}, the consumer is
     * {@code java.util.function.Consumer<io.canvasmc.canvas.WorldUnloadResult>}, which erases to
     * {@link Consumer} and can be implemented without the type of Canvas.
     */
    @Override
    public CompletableFuture<Feedback> unloadWorld(Plugin plugin, World world, boolean save) {
        CompletableFuture<Feedback> future = new CompletableFuture<>();
        try {
            Method unloadWorldAsync = Server.class.getMethod("unloadWorldAsync", World.class, boolean.class, Consumer.class);
            Consumer<Object> callback = result -> future.complete(unloadFeedback(result));
            unloadWorldAsync.invoke(Bukkit.getServer(), world, save, callback);
        } catch (ReflectiveOperationException exception) {
            future.completeExceptionally(exception);
        }
        return future;
    }
}
