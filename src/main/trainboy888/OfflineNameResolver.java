package trainboy888;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineNameResolver {
    private final Plugin plugin;
    private final File cacheFile;
    private final Map<UUID, String> nameCache = new HashMap<>();

    public OfflineNameResolver(Plugin plugin) {
        this.plugin = plugin;
        this.cacheFile = new File(plugin.getDataFolder(), "name-cache.yml");
        load();
    }

    public void load() {
        if (!cacheFile.exists()) {
            save();
            return;
        }

        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(cacheFile);
            for (String uuidKey : configuration.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidKey);
                    String name = configuration.getString(uuidKey);
                    nameCache.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID key, skip
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to load name cache: " + exception.getMessage());
        }
    }

    public void save() {
        try {
            Configuration configuration = new Configuration();
            for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
                configuration.set(entry.getKey().toString(), entry.getValue());
            }
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, cacheFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save name cache: " + exception.getMessage());
        }
    }

    public void cachePlayer(UUID uuid, String name) {
        if (!nameCache.containsKey(uuid) || !nameCache.get(uuid).equals(name)) {
            nameCache.put(uuid, name);
            save();
        }
    }

    public String resolveName(UUID uuid) {
        // First check if online
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(uuid);
        if (online != null) {
            cachePlayer(uuid, online.getName());
            return online.getName();
        }

        // Check cache
        String cached = nameCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        // Return UUID if not found
        return uuid.toString();
    }

    public UUID resolveUUID(String nameOrUuid) {
        // Try parsing as UUID
        try {
            return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, continue
        }

        // Check if online
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(nameOrUuid);
        if (online != null) {
            UUID uuid = online.getUniqueId();
            cachePlayer(uuid, online.getName());
            return uuid;
        }

        // Search cache for name match
        for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nameOrUuid)) {
                return entry.getKey();
            }
        }

        return null;
    }
}
