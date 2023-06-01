package me.hsgamer.morefoworld;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class Permissions {
    public static final Permission TELEPORT = new Permission("morefoworld.teleport", PermissionDefault.OP);
    public static final Permission TELEPORT_OTHERS = new Permission("morefoworld.teleport.others", PermissionDefault.OP);
    public static final Permission CURRENT_WORLD = new Permission("morefoworld.current", PermissionDefault.OP);
    public static final Permission CURRENT_WORLD_OTHERS = new Permission("morefoworld.current.others", PermissionDefault.OP);

    private Permissions() {
        // EMPTY
    }
}
