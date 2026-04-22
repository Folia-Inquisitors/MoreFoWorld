# MoreFoWorld

A reference world manager for Folia

## Why is it a reference world manager?

This plugin was created sorely as a demonstration to see whether it's possible to add customized worlds in Folia aside from the default worlds. 
This plugin doesn't claim to be a full-fledged world manager like others such as [Multiverse Core](https://modrinth.com/plugin/multiverse-core) or [Worlds](https://modrinth.com/plugin/worlds-1), but rather a reference plugin for other plugins to check, tweak and implement the logic on their own.

## Hacky Patch

> These are the features that are currently not supported by Folia, but work on MoreFoWorld with some hacks.
> They don't guarantee to work similarly to Vanilla Minecraft. Use them at your own risk.

- Respawn World
- Portal Teleporting

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
| `/mfw setworldspawn`                                  | `morefoworld.setworldspawn`   | Set the spawn point of the current world           |
