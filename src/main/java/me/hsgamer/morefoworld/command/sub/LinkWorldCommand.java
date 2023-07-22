package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class LinkWorldCommand extends SubCommand {
    private final int argCount;

    protected LinkWorldCommand(String name, String description, int argCount, String argsUsage, String permission) {
        super(name, description, "/<label> " + name + " <from> <to> " + argsUsage, permission, true);
        this.argCount = argCount;
    }

    protected abstract void onWorldCommand(CommandSender sender, World from, World to, String... args);

    protected abstract List<String> onWorldTabComplete(CommandSender sender, String... args);

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        World from = Bukkit.getWorld(args[0]);
        if (from == null) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[1] + " &cis not found");
            return;
        }

        World to = Bukkit.getWorld(args[1]);
        if (to == null) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[2] + " &cis not found");
            return;
        }

        if (from.equals(to)) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[1] + " &cand &e" + args[2] + " &care the same");
            return;
        }

        onWorldCommand(sender, from, to, Arrays.copyOfRange(args, 2, args.length));
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= argCount + 2;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1 || args.length == 2) {
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
        } else if (args.length > 2) {
            return onWorldTabComplete(sender, Arrays.copyOfRange(args, 2, args.length));
        }
        return Collections.emptyList();
    }
}
