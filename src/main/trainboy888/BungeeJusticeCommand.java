package trainboy888;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Map;

public class BungeeJusticeCommand extends Command {
    private final Plugin plugin;
    private final MessageConfig messageConfig;
    private final PunishmentManager punishmentManager;

    public BungeeJusticeCommand(Plugin plugin, MessageConfig messageConfig, PunishmentManager punishmentManager) {
        super("bjustice", "bungeejustice.reload", "bungeejustice");
        this.plugin = plugin;
        this.messageConfig = messageConfig;
        this.punishmentManager = punishmentManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.help-header")));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-punish", Map.of("command", "ban/mute/ipban/ipmute"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-temp-punish", Map.of("command", "tempban/tempmute/tempipban/tempipmute"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-unpunish-id", Map.of("command", "unpunish"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-kick", Map.of("command", "kick"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-warn", Map.of("command", "warn"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-note", Map.of("command", "note"))));
            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-check", Map.of("command", "check"))));
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            // Reload the message/config file and punishment manager
            messageConfig.load();
            punishmentManager.load();

            sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.reload-success", Map.of(
                    "sender", sender.getName()
            ))));
            return;
        }

        sender.sendMessage(new TextComponent(messageConfig.get("messages.help-header")));
        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.help-punish", Map.of("command", "ban/mute/ipban/ipmute"))));
    }
}
