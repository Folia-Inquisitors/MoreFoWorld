# MoreFoWorld

A (temporary) world manager for Folia

_Why does this even exist ??_

## Not Working

- Respawn World
- Portal Teleporting

> Because the current Folia builds don't support them. If one of them does get supported, the related commands will be working.

## Commands & Permissions

| Command                                               | Permission                    | Description                                        |
|-------------------------------------------------------|-------------------------------|----------------------------------------------------|
| `/mfw current`                                        | `morefoworld.current`         | Shows the current world                            |
| `/mfw current <player>`                               | `morefoworld.current.others`  | Shows the current world of another player          |
| `/mfw teleport <world>`                               | `morefoworld.teleport`        | Teleports you to the specified world               |
| `/mfw teleport <world> <player>`                      | `morefoworld.teleport.others` | Teleports another player to the specified world    |
| `/mfw linkworld <from_world> <to_world> <nether/end>` | `morefoworld.linkportal`      | Link Portals between `from_world` and `to_world`   |
| `/mfw unlinkworld <world> <nether/end>`               | `morefoworld.linkportal`      | Un-link Portals of a `world`                       |
| `/mfw linkrespawn <from> <to>`                        | `morefoworld.linkrespawn`     | Link Respawn points between `from` and `to`        |
| `/mfw unlinkrespawn <world>`                          | `morefoworld.linkrespawn`     | Un-link Respawn points of a `world`                |
| `/mfw setspawn`                                       | `morefoworld.setspawn`        | Set the spawn point when a player joins the server |
