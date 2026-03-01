# bungeeJustice

Network-wide BungeeCord moderation plugin for bans, mutes, IP bans, temporary punishments, and removals.

## Features

- Global player bans and mutes
- Global IP bans and IP mutes
- Temporary punishments (`tempban`, `tempmute`, `tempipban`, `tempipmute`)
- Persistent storage in `plugins/bungeeJustice/punishments.yml`

## Commands

- `/ban <player|uuid> [reason]`
- `/tempban <player|uuid> <duration> [reason]`
- `/unban <player|uuid>`
- `/mute <player|uuid> [reason]`
- `/tempmute <player|uuid> <duration> [reason]`
- `/unmute <player|uuid>`
- `/ipban <player|ip> [reason]`
- `/tempipban <player|ip> <duration> [reason]`
- `/unipban <player|ip>`
- `/ipmute <player|ip> [reason]`
- `/tempipmute <player|ip> <duration> [reason]`
- `/unipmute <player|ip>`
- `/banlist`
- `/bjustice reload`

Duration examples: `30m`, `12h`, `7d`, `2w`.

## Permissions

- `bungeejustice.ban`
- `bungeejustice.tempban`
- `bungeejustice.unban`
- `bungeejustice.mute`
- `bungeejustice.tempmute`
- `bungeejustice.unmute`
- `bungeejustice.ipban`
- `bungeejustice.tempipban`
- `bungeejustice.unipban`
- `bungeejustice.ipmute`
- `bungeejustice.tempipmute`
- `bungeejustice.unipmute`
- `bungeejustice.banlist`
- `bungeejustice.reload`