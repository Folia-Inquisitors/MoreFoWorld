package me.hsgamer.morefoworld;

import io.github.projectunified.minelib.plugin.base.BasePlugin;
import io.github.projectunified.minelib.plugin.permission.PermissionComponent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class Permissions extends PermissionComponent {
    public static final Permission TELEPORT = new Permission("morefoworld.teleport", PermissionDefault.OP);
    public static final Permission TELEPORT_OTHERS = new Permission("morefoworld.teleport.others", PermissionDefault.OP);
    public static final Permission CURRENT_WORLD = new Permission("morefoworld.current", PermissionDefault.OP);
    public static final Permission CURRENT_WORLD_OTHERS = new Permission("morefoworld.current.others", PermissionDefault.OP);
    public static final Permission LINK_PORTAL = new Permission("morefoworld.linkportal", PermissionDefault.OP);
    public static final Permission LINK_RESPAWN = new Permission("morefoworld.linkrespawn", PermissionDefault.OP);
    public static final Permission SET_SPAWN = new Permission("morefoworld.setspawn", PermissionDefault.OP);

    public Permissions(BasePlugin plugin) {
        super(plugin);
    }
}
