package trainboy888;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;

public class BungeeJusticeCommand extends Command {
    private final MessageConfig messageConfig;

    public BungeeJusticeCommand(MessageConfig messageConfig) {
        super("bjustice", "bungeejustice.reload", "bungeejustice");
        this.messageConfig = messageConfig;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.no-permission")));
            return;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(new TextComponent(messageConfig.get("messages.usage-reload")));
            return;
        }

        messageConfig.load();
        sender.sendMessage(new TextComponent(messageConfig.getFormatted("messages.reload-success", Map.of(
                "sender", sender.getName()
        ))));
    }
}
