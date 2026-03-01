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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UnpunishCommand extends Command implements TabExecutor {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;
    private final PunishmentType type;
    private final boolean ipTarget;

    public UnpunishCommand(String name, String permission, PunishmentManager manager, MessageConfig messageConfig, PunishmentType type, boolean ipTarget) {
        super(name, permission);
        this.manager = manager;
        this.messageConfig = messageConfig;
        this.type = type;
        this.ipTarget = ipTarget;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.usage-unpunish", Map.of("command", getName()))));
            return;
        }

        if (ipTarget) {
            String ip = resolveIp(args[0]);
            if (ip == null) {
                sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-target-ip")));
                return;
            }

            boolean removed = type == PunishmentType.IP_BAN ? manager.unbanIp(ip) : manager.unmuteIp(ip);
            sender.sendMessage(new TextComponent(removed
                    ? messageConfig.getFormatted("messages.removed-ip", Map.of("target", ip))
                    : messageConfig.getFormatted("messages.no-active-ip", Map.of("target", ip))));
            return;
        }

        UUID uuid = resolveUuid(args[0]);
        if (uuid == null) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-target-player")));
            return;
        }

        boolean removed = type == PunishmentType.BAN ? manager.unbanPlayer(uuid) : manager.unmutePlayer(uuid);
        sender.sendMessage(new TextComponent(removed
                ? messageConfig.getFormatted("messages.removed-player", Map.of("target", args[0]))
                : messageConfig.getFormatted("messages.no-active-player", Map.of("target", args[0]))));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return suggestTargets(args[0]);
        }

        return Collections.emptyList();
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
}
