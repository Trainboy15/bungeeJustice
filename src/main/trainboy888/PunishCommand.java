package trainboy888;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PunishCommand extends Command implements TabExecutor {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;
    private final PunishmentType type;
    private final boolean temporary;
    private final boolean ipTarget;

    public PunishCommand(String name, String permission, PunishmentManager manager, MessageConfig messageConfig, PunishmentType type, boolean temporary, boolean ipTarget) {
        super(name, permission);
        this.manager = manager;
        this.messageConfig = messageConfig;
        this.type = type;
        this.temporary = temporary;
        this.ipTarget = ipTarget;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        int minArgs = temporary ? 2 : 1;
        if (args.length < minArgs) {
            sender.sendMessage(new TextComponent(usage()));
            return;
        }

        long durationMillis = -1;
        int reasonStart = 1;
        if (temporary) {
            durationMillis = DurationParser.parseDurationMillis(args[1]);
            if (durationMillis == 0) {
                sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-duration")));
                return;
            }
            reasonStart = 2;
        }

        String reason = args.length > reasonStart
                ? String.join(" ", Arrays.copyOfRange(args, reasonStart, args.length))
                : messageConfig.get("messages.default-reason");

        String actor = sender.getName();

        if (ipTarget) {
            String ip = resolveIp(args[0]);
            if (ip == null) {
                sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-target-ip")));
                return;
            }

            manager.punishIp(ip, type, reason, actor, durationMillis);
            kickMatchingPlayers(type, ip, reason, actor, durationMillis);

                sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.applied-ip", Map.of(
                    "punishment", readableType(type),
                    "target", ip
                ))));
            return;
        }

        UUID uuid = resolveUuid(args[0]);
        if (uuid == null) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-target-player")));
            return;
        }

        manager.punishPlayer(uuid, type, reason, actor, durationMillis);

        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(uuid);
        if (online != null && type == PunishmentType.BAN) {
            Punishment ban = manager.getActivePlayerBan(uuid);
            if (ban != null) {
                online.disconnect(new TextComponent(messageConfig.formatPunishmentScreen("screens.ban", ban)));
            }
        }

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.applied-player", Map.of(
                "punishment", readableType(type),
                "target", args[0]
        ))));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return suggestTargets(args[0]);
        }

        if (temporary && args.length == 2) {
            return suggestDurations(args[1]);
        }

        return Collections.emptyList();
    }

    private String usage() {
        if (temporary) {
            return messageConfig.getFormatted("messages.usage-temp-punish", Map.of("command", getName()));
        }
        return messageConfig.getFormatted("messages.usage-punish", Map.of("command", getName()));
    }

    private Collection<String> suggestTargets(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            String name = player.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(lower)) {
                suggestions.add(name);
            }
        }

        return suggestions;
    }

    private Collection<String> suggestDurations(String input) {
        String[] common = {"30m", "1h", "12h", "1d", "7d", "2w"};
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        for (String value : common) {
            if (value.startsWith(lower)) {
                suggestions.add(value);
            }
        }

        return suggestions;
    }

    private UUID resolveUuid(String target) {
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(target);
        if (online != null) {
            return online.getUniqueId();
        }

        try {
            return UUID.fromString(target);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String resolveIp(String target) {
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(target);
        if (online != null) {
            InetSocketAddress address = online.getAddress();
            return address.getAddress().getHostAddress();
        }

        try {
            InetAddress parsed = InetAddress.getByName(target);
            return parsed.getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void kickMatchingPlayers(PunishmentType type, String ip, String reason, String actor, long durationMillis) {
        if (type != PunishmentType.IP_BAN) {
            return;
        }

        String normalized = PunishmentManager.normalizeIp(ip);
        Punishment ipBan = manager.getActiveIpBan(ip);
        if (ipBan == null) {
            return;
        }

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            String playerIp = PunishmentManager.normalizeIp(player.getAddress().getAddress().getHostAddress());
            if (!playerIp.equals(normalized)) {
                continue;
            }

            player.disconnect(new TextComponent(messageConfig.formatPunishmentScreen("screens.ip-ban", ipBan)));
        }
    }

    private String readableType(PunishmentType type) {
        switch (type) {
            case BAN:
                return temporary ? "temporary ban" : "ban";
            case MUTE:
                return temporary ? "temporary mute" : "mute";
            case IP_BAN:
                return temporary ? "temporary IP ban" : "IP ban";
            case IP_MUTE:
                return temporary ? "temporary IP mute" : "IP mute";
            default:
                return "punishment";
        }
    }
}
