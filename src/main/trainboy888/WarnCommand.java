package trainboy888;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class WarnCommand extends Command implements TabExecutor {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;
    private final OfflineNameResolver nameResolver;

    public WarnCommand(PunishmentManager manager, MessageConfig messageConfig, OfflineNameResolver nameResolver) {
        super("warn", "bungeejustice.warn");
        this.manager = manager;
        this.messageConfig = messageConfig;
        this.nameResolver = nameResolver;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.usage-warn", Map.of("command", getName()))));
            return;
        }

        UUID uuid = nameResolver.resolveUUID(args[0]);
        if (uuid == null) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.invalid-target-player")));
            return;
        }

        String reason = args.length > 1
                ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
                : messageConfig.get("messages.default-reason");

        String actor = sender.getName();

        // Add warn to player
        manager.punishPlayer(uuid, PunishmentType.WARN, reason, actor, -1);

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.warned-player", Map.of(
                "target", nameResolver.resolveName(uuid)
        ))));

        // Notify if online
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(uuid);
        if (online != null) {
            online.sendMessage(new TextComponent(messageConfig.get("screens.warn")
                    .replace("{reason}", reason)
                    .replace("{actor}", actor)));
        }
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
