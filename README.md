# bungeeJustice

Network-wide BungeeCord moderation plugin for bans, mutes, IP bans, temporary punishments, and removals.

## Features

- Global player bans and mutes
- Global IP bans and IP mutes
- Temporary punishments (`tempban`, `tempmute`, `tempipban`, `tempipmute`)
- Kick, warn, and note commands for player management
- Unified unpunish system with ID-based removal
- Persistent storage in `plugins/bungeeJustice/punishments.yml`
- Configurable messages and commands

## Commands

- `/ban <player|uuid> [reason]` - Ban a player
- `/tempban <player|uuid> <duration> [reason]` - Temporarily ban a player
- `/mute <player|uuid> [reason]` - Mute a player
- `/tempmute <player|uuid> <duration> [reason]` - Temporarily mute a player
- `/ipban <player|ip> [reason]` - Ban a player's IP
- `/tempipban <player|ip> <duration> [reason]` - Temporarily ban an IP
- `/ipmute <player|ip> [reason]` - Mute a player's IP
- `/tempipmute <player|ip> <duration> [reason]` - Temporarily mute an IP
- `/unpunish <id>` - Remove a punishment by its ID
- `/kick <player> [reason]` - Kick a player
- `/warn <player> [reason]` - Warn a player
- `/note <player> [note]` - Add a note about a player
- `/banlist [id]` - View active punishments or details of a specific punishment
- `/bjustice reload` - Reload plugin configuration

Duration examples: `30m`, `12h`, `7d`, `2w`

## Permissions

- `bungeejustice.ban`
- `bungeejustice.tempban`
- `bungeejustice.unpunish`
- `bungeejustice.mute`
- `bungeejustice.tempmute`
- `bungeejustice.kick`
- `bungeejustice.warn`
- `bungeejustice.note`
- `bungeejustice.ipban`
- `bungeejustice.tempipban`
- `bungeejustice.ipmute`
- `bungeejustice.tempipmute`
- `bungeejustice.banlist`
- `bungeejustice.reload`