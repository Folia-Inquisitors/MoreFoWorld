package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.command.sub.SubCommand;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class UnlinkPortalCommand extends SubCommand {
    private final MoreFoWorld plugin;

    public UnlinkPortalCommand(MoreFoWorld plugin) {
        super("unlinkportal", "Unlink the portal of a world", "/<label> unlinkportal <world> <nether/end>", Permissions.LINK_PORTAL.getName(), true);
        this.plugin = plugin;
    }

    @Override
    public void onSubCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            MessageUtils.sendMessage(sender, "&cThe world &e" + args[1] + " &cis not found");
            return;
        }

        Predicate<String> action;
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "nether" -> action = plugin.getPortalConfig()::unlinkNetherPortal;
            case "end" -> action = plugin.getPortalConfig()::unlinkEndPortal;
            default -> {
                MessageUtils.sendMessage(sender, "&cInvalid type: &e" + args[3]);
                return;
            }
        }

        if (action.test(world.getName())) {
            MessageUtils.sendMessage(sender, "&aSuccessfully unlinked");
        } else {
            MessageUtils.sendMessage(sender, "&cFailed to unlink. Is the portal already unlinked?");
        }
    }

    @Override
    public boolean isProperUsage(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        return args.length >= 2;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String... args) {
        if (args.length == 1) {
            return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
        } else if (args.length == 2) {
            return List.of("nether", "end");
        }
        return Collections.emptyList();
    }
}
