package me.hsgamer.morefoworld;

import io.github.projectunified.craftcommand.CommandInfo;
import io.github.projectunified.craftcommand.annotation.*;
import io.github.projectunified.craftcommand.bukkit.annotation.Permission;
import io.github.projectunified.craftcommand.paper.PaperCommandManager;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.config.PortalConfig;
import me.hsgamer.morefoworld.config.RespawnConfig;
import me.hsgamer.morefoworld.config.SpawnConfig;
import me.hsgamer.morefoworld.config.WorldSpawnConfig;
import me.hsgamer.morefoworld.config.object.WorldPosition;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.logging.Level;

@Command(value = "morefoworld", aliases = {"worlds", "mfw"}, description = "MoreFoWorld command")
public class Commands {
    private final MoreFoWorld plugin;

    public Commands(MoreFoWorld plugin) {
        this.plugin = plugin;
    }

    @Default
    public void printInfo(CommandSender sender) {
        for (CommandInfo commandInfo : plugin.get(PaperCommandManager.class).getCommandInfo(this)) {
            MessageUtils.sendMessage(sender, "&e" + String.join(" ", commandInfo.getPath()) + " &b" + commandInfo.getUsage(), "");
            MessageUtils.sendMessage(sender, "  &f" + commandInfo.getDescription(), "");
        }
    }

    @Subcommand(value = "current", description = "Get the world of the player")
    @Permission("morefoworld.current")
    public void current(CommandSender sender, @Optional Player target) {
        if (target == null) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                throw new CommandException("You must be a player to do this");
            }
        }
        if (target != sender && !sender.hasPermission(Permissions.CURRENT_WORLD_OTHERS)) {
            throw new CommandException("You don't have permission to check the world of other players");
        }
        MessageUtils.sendMessage(sender, "&aThe current world of &e" + target.getName() + " &a is &e" + target.getWorld().getName());
    }

    @Subcommand(value = "teleport", description = "Teleport to a world")
    @Permission("morefoworld.teleport")
    public void teleport(CommandSender sender, World world, @Optional Player target) {
        if (target == null) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                throw new CommandException("You must be a player to do this");
            }
        }
        if (target != sender && !sender.hasPermission(Permissions.TELEPORT_OTHERS)) {
            throw new CommandException("You don't have permission to teleport other players");
        }
        MessageUtils.sendMessage(sender, "&aTeleporting...");
        target.teleportAsync(world.getSpawnLocation()).whenComplete((_, throwable) -> {
            if (throwable != null) {
                MessageUtils.sendMessage(sender, "&cAn error occurred: " + throwable.getMessage());
                plugin.getLogger().log(Level.WARNING, "An error occurred while teleporting the player", throwable);
            } else {
                MessageUtils.sendMessage(sender, "&aYou have been teleported to &e" + world.getName());
            }
        });
    }

    public final List<String> portalTypes = Arrays.asList("nether", "end");

    @Subcommand(value = "linkportal", description = "Link portals between two worlds")
    @Permission("morefoworld.linkportal")
    public void linkPortal(CommandSender sender, World from, World to, @Suggest("portalTypes") String type) {
        BiConsumer<String, String> action;
        switch (type.toLowerCase(Locale.ROOT)) {
            case "nether" -> action = plugin.get(PortalConfig.class)::linkNetherPortal;
            case "end" -> action = plugin.get(PortalConfig.class)::linkEndPortal;
            default -> throw new CommandException("Invalid type: " + type);
        }
        action.accept(from.getName(), to.getName());
        MessageUtils.sendMessage(sender, "&aSuccessfully linked");
    }

    @Subcommand(value = "unlinkportal", description = "Unlink the portal of a world")
    @Permission("morefoworld.linkportal")
    public void unlinkPortal(CommandSender sender, World world, @Suggest("portalTypes") String type) {
        Predicate<String> action;
        switch (type.toLowerCase(Locale.ROOT)) {
            case "nether" -> action = plugin.get(PortalConfig.class)::unlinkNetherPortal;
            case "end" -> action = plugin.get(PortalConfig.class)::unlinkEndPortal;
            default -> throw new CommandException("Invalid type: " + type);
        }
        if (action.test(world.getName())) {
            MessageUtils.sendMessage(sender, "&aSuccessfully unlinked");
        } else {
            MessageUtils.sendMessage(sender, "&cFailed to unlink. Is the portal already unlinked?");
        }
    }

    @Subcommand(value = "linkrespawn", description = "Link respawn location between two worlds")
    @Permission("morefoworld.linkrespawn")
    public void linkRespawn(CommandSender sender, World from, World to) {
        plugin.get(RespawnConfig.class).linkWorld(from.getName(), to.getName());
        MessageUtils.sendMessage(sender, "&aSuccessfully linked");
    }

    @Subcommand(value = "unlinkrespawn", description = "Unlink respawn location of a world")
    @Permission("morefoworld.linkrespawn")
    public void unlinkRespawn(CommandSender sender, World world) {
        if (plugin.get(RespawnConfig.class).unlinkWorld(world.getName())) {
            MessageUtils.sendMessage(sender, "&aSuccessfully unlinked");
        } else {
            MessageUtils.sendMessage(sender, "&cFailed to unlink. Is the respawn location already unlinked?");
        }
    }

    @Subcommand(value = "setspawn", description = "Set the spawn location the player will be teleported to when they joins the server")
    @Permission("morefoworld.setspawn")
    public void setSpawn(Player player) {
        SpawnConfig spawnConfig = plugin.get(SpawnConfig.class);
        spawnConfig.setPosition(WorldPosition.fromLocation(player.getLocation()));
        if (!spawnConfig.isEnabled()) {
            spawnConfig.setEnabled(true);
        }
        MessageUtils.sendMessage(player, "&aSpawn location set");
    }

    @Subcommand(value = "setworldspawn", description = "Set the spawn location of the world")
    @Permission("morefoworld.setworldspawn")
    public void setWorldSpawn(Player player) {
        WorldPosition worldPosition = WorldPosition.fromLocation(player.getLocation());
        plugin.get(WorldSpawnConfig.class).setSpawn(worldPosition);
        WorldUtil.applyWorldSpawn(worldPosition.toLocation());
        MessageUtils.sendMessage(player, "&aWorld spawn location set");
    }
}
