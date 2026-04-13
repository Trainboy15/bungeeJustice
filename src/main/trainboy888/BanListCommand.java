package trainboy888;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;
import java.util.UUID;

public class BanListCommand extends Command {
    private final PunishmentManager punishmentManager;
    private final MessageConfig messageConfig;
    private final OfflineNameResolver nameResolver;

    public BanListCommand(PunishmentManager punishmentManager, MessageConfig messageConfig, OfflineNameResolver nameResolver) {
        super("banlist", "bungeejustice.banlist");
        this.punishmentManager = punishmentManager;
        this.messageConfig = messageConfig;
        this.nameResolver = nameResolver;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        // If they provide an ID, look up that specific punishment
        if (args.length > 0) {
            String id = args[0];
            Punishment punishment = punishmentManager.getById(id);
            if (punishment == null) {
                sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishment-not-found", Map.of("id", id))));
                return;
            }
            
            String targetDisplay = getTargetDisplay(punishment);
            
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishment-info", Map.of(
                    "id", punishment.getId(),
                    "player", targetDisplay,
                    "type", getPunishmentTypeName(punishment.getType()),
                    "reason", punishment.getReason(),
                    "actor", punishment.getActor(),
                    "created", formatTime(punishment.getCreatedAt()),
                    "duration", formatDuration(punishment)
            ))));
            return;
        }

        // Show all active punishments
        Map<String, Punishment> allPunishments = punishmentManager.getAllPunishments();

        if (allPunishments.isEmpty()) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.punishlist-empty")));
            return;
        }

        // Count punishments excluding KICKs
        long displayCount = allPunishments.values().stream()
                .filter(p -> p.getType() != PunishmentType.KICK)
                .count();

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishlist-header", Map.of(
                "count", String.valueOf(displayCount)
        ))));

        allPunishments.entrySet().stream()
                .sorted((left, right) -> comparePunishmentIdsDesc(left.getValue().getId(), right.getValue().getId()))
                .forEach(entry -> {
            Punishment punishment = entry.getValue();
            
            // Skip KICK punishments
            if (punishment.getType() == PunishmentType.KICK) {
                return;
            }
            
            String typeDisplayName = getPunishmentTypeName(punishment.getType());
            String targetDisplay = getTargetDisplay(punishment);

            String entryMessage = messageConfig.getFormatted("messages.punishlist-entry", Map.of(
                    "id", punishment.getId(),
                    "player", targetDisplay,
                    "type", typeDisplayName,
                    "reason", punishment.getReason(),
                    "actor", punishment.getActor(),
                    "duration", formatDuration(punishment)
            ));

            sender.sendMessage(createInteractiveEntry(entryMessage, punishment.getId()));
        });
    }

    private int comparePunishmentIdsDesc(String leftId, String rightId) {
        try {
            return Long.compare(Long.parseLong(rightId), Long.parseLong(leftId));
        } catch (NumberFormatException ignored) {
            return rightId.compareTo(leftId);
        }
    }

    private TextComponent createInteractiveEntry(String entryMessage, String punishmentId) {
        TextComponent entryComponent = new TextComponent(entryMessage);
        entryComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text("Click to view details for punishment #" + punishmentId)));
        entryComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/banlist " + punishmentId));
        return entryComponent;
    }

    private String getTargetDisplay(Punishment punishment) {
        // Resolve IP punishments to a known player name when available.
        if (punishment.getType() == PunishmentType.IP_BAN || punishment.getType() == PunishmentType.IP_MUTE) {
            String ip = punishment.getTarget();
            String resolvedName = nameResolver.resolveNameByIp(ip);
            if (resolvedName != null) {
                return resolvedName + " (" + ip + ")";
            }
            return ip;
        }

        // For player punishments, resolve UUID to name.
        UUID playerUUID = punishment.getPlayerUUID();
        if (playerUUID != null) {
            String resolvedName = nameResolver.resolveName(playerUUID);
            if (!resolvedName.equals(playerUUID.toString())) {
                return resolvedName;
            }
        }

        // Fallback to target string
        return punishment.getTarget();
    }

    private String getPunishmentTypeName(PunishmentType type) {
        switch (type) {
            case BAN:
                return "Ban";
            case MUTE:
                return "Mute";
            case IP_BAN:
                return "IP Ban";
            case IP_MUTE:
                return "IP Mute";
            case WARN:
                return "Warn";
            case NOTE:
                return "Note";
            default:
                return "Unknown";
        }
    }

    private String formatDuration(Punishment punishment) {
        if (punishment.isPermanent()) {
            return "Permanent";
        }

        long remaining = Math.max(0L, punishment.getExpiresAt() - System.currentTimeMillis());
        return DurationParser.formatDuration(remaining);
    }

    private String formatTime(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
}
