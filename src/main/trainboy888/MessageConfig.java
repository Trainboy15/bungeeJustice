package trainboy888;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageConfig {
    private final Plugin plugin;
    private final File configFile;
    private Configuration config;

    public MessageConfig(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }

        if (!configFile.exists()) {
            if (!copyBundledConfig()) {
                writeDefaultConfig();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to load config.yml: " + exception.getMessage());
            config = new Configuration();
        }
    }

    public String get(String path) {
        String value = config.getString(path, "");
        value = value.replace("\\n", "\n");
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String getFormatted(String path, Map<String, String> placeholders) {
        String message = get(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public String formatPunishmentScreen(String path, Punishment punishment) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", punishment.getReason());
        placeholders.put("actor", punishment.getActor());

        String duration = punishment.isPermanent()
                ? "Permanent"
                : DurationParser.formatDuration(Math.max(0, punishment.getExpiresAt() - System.currentTimeMillis()));
        placeholders.put("duration", duration);

        return getFormatted(path, placeholders);
    }

    private boolean copyBundledConfig() {
        try (InputStream input = plugin.getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (input == null) {
                return false;
            }

            try (FileOutputStream output = new FileOutputStream(configFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            return true;
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to copy bundled config.yml: " + exception.getMessage());
            return false;
        }
    }

    private void writeDefaultConfig() {
        String defaults = "messages:\n"
                + "  no-permission: '&cNo permission.'\n"
                + "  invalid-duration: '&cInvalid duration. Example: 30m, 12h, 7d, 2w.'\n"
                + "  invalid-target-player: '&cTarget must be an online player or UUID.'\n"
                + "  invalid-target-ip: '&cTarget must be an online player or valid IP address.'\n"
                + "  default-reason: 'No reason provided'\n"
                + "  usage-punish: '&e/{command} <target> [reason]'\n"
                + "  usage-temp-punish: '&e/{command} <target> <duration> [reason]'\n"
                + "  usage-unpunish: '&e/{command} <target>'\n"
                + "  applied-player: '&aApplied {punishment} to {target}.'\n"
                + "  applied-ip: '&aApplied {punishment} to IP {target}.'\n"
                + "  removed-player: '&aRemoved punishment from {target}.'\n"
                + "  removed-ip: '&aRemoved punishment from IP {target}.'\n"
                + "  no-active-player: '&cNo active punishment found for {target}.'\n"
                + "  no-active-ip: '&cNo active punishment found for IP {target}.'\n"
                + "  banlist-empty: '&eThere are no active bans.'\\n"
                + "  banlist-header: '&6Active bans: &f{count}'\\n"
                + "  banlist-player-section: '&ePlayer bans:'\\n"
                + "  banlist-player-entry: '&7- &f{target} &8| &7Reason: &f{reason} &8| &7By: &f{actor} &8| &7Length: &f{duration}'\\n"
                + "  banlist-ip-section: '&eIP bans:'\\n"
                + "  banlist-ip-entry: '&7- &f{target} &8| &7Reason: &f{reason} &8| &7By: &f{actor} &8| &7Length: &f{duration}'\\n"
                + "  usage-reload: '&e/bjustice reload'\\n"
                + "  reload-success: '&aReloaded bungeeJustice config.'\\n"
                + "screens:\n"
                + "  ban: '&cYou are banned on this network.\\n&7Reason: &f{reason}\\n&7By: &f{actor}\\n&7Length: &f{duration}'\n"
                + "  ip-ban: '&cYou are IP-banned on this network.\\n&7Reason: &f{reason}\\n&7By: &f{actor}\\n&7Length: &f{duration}'\n"
                + "  mute: '&cYou are muted on this network.\\n&7Reason: &f{reason}\\n&7By: &f{actor}\\n&7Length: &f{duration}'\n"
                + "  ip-mute: '&cYour IP is muted on this network.\\n&7Reason: &f{reason}\\n&7By: &f{actor}\\n&7Length: &f{duration}'\n";

        try (FileOutputStream output = new FileOutputStream(configFile)) {
            output.write(defaults.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to write default config.yml: " + exception.getMessage());
        }
    }
}
