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

public class NoteCommand extends Command implements TabExecutor {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;
    private final OfflineNameResolver nameResolver;

    public NoteCommand(PunishmentManager manager, MessageConfig messageConfig, OfflineNameResolver nameResolver) {
        super("note", "bungeejustice.note");
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
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.usage-note", Map.of("command", getName()))));
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

        // Add note to player (internal record only, not shown to player)
        manager.punishPlayer(uuid, PunishmentType.NOTE, reason, actor, -1);

        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.noted-player", Map.of(
                "target", nameResolver.resolveName(uuid)
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
