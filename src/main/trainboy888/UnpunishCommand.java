package trainboy888;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Map;

public class UnpunishCommand extends Command implements TabExecutor {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;

    public UnpunishCommand(PunishmentManager manager, MessageConfig messageConfig) {
        super("unpunish", "bungeejustice.unpunish");
        this.manager = manager;
        this.messageConfig = messageConfig;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.usage-unpunish-id", Map.of("command", getName()))));
            return;
        }

        String id = args[0];
        Punishment punishment = manager.getById(id);

        if (punishment == null) {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishment-not-found", Map.of("id", id))));
            return;
        }

        boolean removed = manager.removeById(id);
        if (removed) {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishment-removed", Map.of(
                    "id", id,
                    "type", punishment.getType().toString()
            ))));
        } else {
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.punishment-removal-failed", Map.of("id", id))));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return Collections.emptyList();
        }

        // No tab completion for IDs
        return Collections.emptyList();
    }
}
