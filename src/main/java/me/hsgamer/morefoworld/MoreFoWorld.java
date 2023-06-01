package me.hsgamer.morefoworld;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.config.proxy.ConfigGenerator;
import me.hsgamer.morefoworld.command.MainCommand;
import me.hsgamer.morefoworld.config.MainConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import static java.util.logging.Level.WARNING;

public final class MoreFoWorld extends BasePlugin {
    private final MainConfig mainConfig = ConfigGenerator.newInstance(MainConfig.class, new BukkitConfig(this));

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
