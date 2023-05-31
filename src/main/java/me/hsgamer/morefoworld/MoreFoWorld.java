package me.hsgamer.morefoworld;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.WARNING;

public final class MoreFoWorld extends JavaPlugin implements Listener {
    private final List<CraftWorld> worlds = new ArrayList<>();

    @Override
    public void onLoad() {
        WorldUtil.FeedbackWorld feedbackWorld = WorldUtil.addWorld(WorldCreator.name("test"));
        getLogger().info("World: " + feedbackWorld.world);
        getLogger().info("Feedback: " + feedbackWorld.feedback);
        if (feedbackWorld.feedback == WorldUtil.Feedback.SUCCESS) {
            worlds.add(feedbackWorld.world);
        }
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component component = event.originalMessage();
        String message = PlainTextComponentSerializer.plainText().serialize(component);
        String lowerCase = message.toLowerCase();
        if (lowerCase.contains("current world")) {
            player.sendMessage("Current world: " + player.getWorld().getName());
        } else if (lowerCase.startsWith("teleport world ")) {
            String worldName = message.substring("teleport world ".length());
            World world = getServer().getWorld(worldName);
            if (world == null) {
                player.sendMessage("World not found");
            } else {
                player.teleportAsync(world.getSpawnLocation()).whenComplete((aVoid, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage("Error: " + throwable.getMessage());
                        getLogger().log(WARNING, "Error", throwable);
                    } else {
                        player.sendMessage("Teleported");
                    }
                });
            }
        }
    }
}
