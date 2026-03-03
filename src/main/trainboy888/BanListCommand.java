package trainboy888;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishlist-header", Map.of(
                "count", String.valueOf(allPunishments.size())
        ))));

        for (Map.Entry<String, Punishment> entry : allPunishments.entrySet()) {
            Punishment punishment = entry.getValue();
            String typeDisplayName = getPunishmentTypeName(punishment.getType());
            String targetDisplay = getTargetDisplay(punishment);
            
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishlist-entry", Map.of(
                    "id", punishment.getId(),
                    "player", targetDisplay,
                    "type", typeDisplayName,
                    "reason", punishment.getReason(),
                    "actor", punishment.getActor(),
                    "duration", formatDuration(punishment)
            ))));
        }
    }

    private String getTargetDisplay(Punishment punishment) {
        // Check if it's an IP punishment
        if (punishment.getType() == PunishmentType.IP_BAN || punishment.getType() == PunishmentType.IP_MUTE) {
            return punishment.getTarget(); // Return IP address directly
        }
        
        // For player punishments, resolve UUID to name
        UUID playerUUID = punishment.getPlayerUUID();
        if (playerUUID != null) {
            return nameResolver.resolveName(playerUUID);
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
