package me.hsgamer.morefoworld.command.sub;

import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.morefoworld.MoreFoWorld;
import me.hsgamer.morefoworld.Permissions;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class UnlinkPortalCommand extends UnlinkWorldCommand {
    private final MoreFoWorld plugin;

    public UnlinkPortalCommand(MoreFoWorld plugin) {
        super("unlinkportal", "Unlink the portal of a world", 1, "<nether/end>", Permissions.LINK_PORTAL.getName());
        this.plugin = plugin;
    }

    @Override
    protected void onWorldCommand(CommandSender sender, World world, String... args) {
        Predicate<String> action;
        switch (args[0].toLowerCase(Locale.ROOT)) {
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
    protected List<String> onWorldTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return List.of("nether", "end");
        }
        return Collections.emptyList();
    }
}
