package me.hsgamer.morefoworld.command;

import me.hsgamer.hscore.bukkit.command.sub.SubCommandManager;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.command.sub.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MainCommand extends Command {
    private final SubCommandManager subCommandManager;

    public MainCommand(@NotNull MoreFoWorld plugin) {
        super("morefoworld", "MoreFoWorld command", "/morefoworld", Arrays.asList("worlds", "mfw"));
        this.subCommandManager = new SubCommandManager();
        subCommandManager.registerSubcommand(new CurrentCommand());
        subCommandManager.registerSubcommand(new TeleportCommand(plugin));
        subCommandManager.registerSubcommand(new LinkPortalCommand(plugin));
        subCommandManager.registerSubcommand(new UnlinkPortalCommand(plugin));
        subCommandManager.registerSubcommand(new LinkRespawnCommand(plugin));
        subCommandManager.registerSubcommand(new UnlinkRespawnCommand(plugin));
        subCommandManager.registerSubcommand(new SetSpawnCommand(plugin));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return subCommandManager.onCommand(sender, commandLabel, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return subCommandManager.onTabComplete(sender, alias, args);
    }
}
