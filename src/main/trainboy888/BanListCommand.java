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

    public BanListCommand(PunishmentManager punishmentManager, MessageConfig messageConfig) {
        super("banlist", "bungeejustice.banlist");
        this.punishmentManager = punishmentManager;
        this.messageConfig = messageConfig;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        Map<UUID, Punishment> playerBans = punishmentManager.getActivePlayerBans();
        Map<String, Punishment> ipBans = punishmentManager.getActiveIpBans();
        int total = playerBans.size() + ipBans.size();

        if (total == 0) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.banlist-empty")));
            return;
        }

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.banlist-header", Map.of(
                "count", String.valueOf(total)
        ))));

        if (!playerBans.isEmpty()) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.banlist-player-section")));
            for (Map.Entry<UUID, Punishment> entry : playerBans.entrySet()) {
                String target = resolveName(entry.getKey());
                Punishment punishment = entry.getValue();
                sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.banlist-player-entry", Map.of(
                        "target", target,
                        "reason", punishment.getReason(),
                        "actor", punishment.getActor(),
                        "duration", formatDuration(punishment)
                ))));
            }
        }

        if (!ipBans.isEmpty()) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.banlist-ip-section")));
            for (Map.Entry<String, Punishment> entry : ipBans.entrySet()) {
                Punishment punishment = entry.getValue();
                sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.banlist-ip-entry", Map.of(
                        "target", entry.getKey(),
                        "reason", punishment.getReason(),
                        "actor", punishment.getActor(),
                        "duration", formatDuration(punishment)
                ))));
            }
        }
    }

    private String resolveName(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        return player != null ? player.getName() : uuid.toString();
    }

    private String formatDuration(Punishment punishment) {
        if (punishment.isPermanent()) {
            return "Permanent";
        }

        long remaining = Math.max(0L, punishment.getExpiresAt() - System.currentTimeMillis());
        return DurationParser.formatDuration(remaining);
    }
}
