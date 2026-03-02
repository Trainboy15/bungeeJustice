package trainboy888;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.plugin.Listener;

import java.net.InetSocketAddress;
import java.util.UUID;

public class PunishmentListener implements Listener {
    private final PunishmentManager manager;
    private final MessageConfig messageConfig;
    private final OfflineNameResolver nameResolver;

    public PunishmentListener(PunishmentManager manager, MessageConfig messageConfig, OfflineNameResolver nameResolver) {
        this.manager = manager;
        this.messageConfig = messageConfig;
        this.nameResolver = nameResolver;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        String ip = ((InetSocketAddress) event.getConnection().getSocketAddress()).getAddress().getHostAddress();

        Punishment playerBan = manager.getActivePlayerBan(uuid);
        Punishment ipBan = manager.getActiveIpBan(ip);
        Punishment effectiveBan = playerBan != null ? playerBan : ipBan;

        if (effectiveBan != null) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(formatMessage(effectiveBan)));
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        // Cache the player name for offline name resolution
        nameResolver.cachePlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer) || event.isCommand()) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String ip = player.getAddress().getAddress().getHostAddress();

        Punishment playerMute = manager.getActivePlayerMute(player.getUniqueId());
        Punishment ipMute = manager.getActiveIpMute(ip);
        Punishment effectiveMute = playerMute != null ? playerMute : ipMute;

        if (effectiveMute != null) {
            event.setCancelled(true);
            player.sendMessage(new TextComponent(formatMessage(effectiveMute)));
        }
    }

    private String formatMessage(Punishment punishment) {
        switch (punishment.getType()) {
            case BAN:
                return messageConfig.formatPunishmentScreen("screens.ban", punishment);
            case IP_BAN:
                return messageConfig.formatPunishmentScreen("screens.ip-ban", punishment);
            case MUTE:
                return messageConfig.formatPunishmentScreen("screens.mute", punishment);
            case IP_MUTE:
                return messageConfig.formatPunishmentScreen("screens.ip-mute", punishment);
            default:
                return messageConfig.formatPunishmentScreen("screens.ban", punishment);
        }
    }
}
