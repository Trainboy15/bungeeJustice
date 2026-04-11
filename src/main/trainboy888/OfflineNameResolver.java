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
    private final Map<UUID, String> ipCache = new HashMap<>();

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
                    Object value = configuration.get(uuidKey);
                    if (value instanceof Configuration) {
                        Configuration playerData = (Configuration) value;
                        String name = playerData.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            nameCache.put(uuid, name);
                        }

                        String ip = playerData.getString("ip");
                        if (ip != null && !ip.trim().isEmpty()) {
                            ipCache.put(uuid, PunishmentManager.normalizeIp(ip));
                        }
                    } else if (value instanceof String) {
                        // Backward compatibility with old flat format (uuid: name)
                        String legacyName = (String) value;
                        if (!legacyName.trim().isEmpty()) {
                            nameCache.put(uuid, legacyName);
                        }
                    }
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
            for (UUID uuid : nameCache.keySet()) {
                Configuration playerData = new Configuration();
                playerData.set("name", nameCache.get(uuid));

                String ip = ipCache.get(uuid);
                if (ip != null && !ip.trim().isEmpty()) {
                    playerData.set("ip", ip);
                }

                configuration.set(uuid.toString(), playerData);
            }

            // Persist IP-only entries if the name is not cached yet.
            for (Map.Entry<UUID, String> ipEntry : ipCache.entrySet()) {
                UUID uuid = ipEntry.getKey();
                if (configuration.get(uuid.toString()) != null) {
                    continue;
                }

                Configuration playerData = new Configuration();
                playerData.set("ip", ipEntry.getValue());
                configuration.set(uuid.toString(), playerData);
            }

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, cacheFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save name cache: " + exception.getMessage());
        }
    }

    public void cachePlayer(UUID uuid, String name) {
        if (uuid == null || name == null || name.trim().isEmpty()) {
            return;
        }

        if (!nameCache.containsKey(uuid) || !nameCache.get(uuid).equals(name)) {
            nameCache.put(uuid, name);
            save();
        }
    }

    public void cachePlayer(UUID uuid, String name, String ip) {
        if (uuid == null) {
            return;
        }

        boolean changed = false;
        if (name != null && !name.trim().isEmpty() && !name.equals(nameCache.get(uuid))) {
            nameCache.put(uuid, name);
            changed = true;
        }

        if (ip != null && !ip.trim().isEmpty()) {
            String normalizedIp = PunishmentManager.normalizeIp(ip);
            if (!normalizedIp.equals(ipCache.get(uuid))) {
                ipCache.put(uuid, normalizedIp);
                changed = true;
            }
        }

        if (changed) {
            save();
        }
    }

    public String resolveName(UUID uuid) {
        // First check if online
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(uuid);
        if (online != null) {
            String onlineIp = online.getAddress() != null && online.getAddress().getAddress() != null
                    ? online.getAddress().getAddress().getHostAddress()
                    : null;
            cachePlayer(uuid, online.getName(), onlineIp);
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
        if (nameOrUuid == null || nameOrUuid.trim().isEmpty()) {
            return null;
        }

        String normalizedInput = nameOrUuid.trim();

        // Try parsing as UUID
        try {
            return UUID.fromString(normalizedInput);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, continue
        }

        // Check if online
        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(normalizedInput);
        if (online != null) {
            UUID uuid = online.getUniqueId();
            String onlineIp = online.getAddress() != null && online.getAddress().getAddress() != null
                    ? online.getAddress().getAddress().getHostAddress()
                    : null;
            cachePlayer(uuid, online.getName(), onlineIp);
            return uuid;
        }

        // Search cache for name match
        for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(normalizedInput)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public String resolveLastIp(String nameOrUuid) {
        UUID uuid = resolveUUID(nameOrUuid);
        if (uuid == null) {
            return null;
        }

        ProxiedPlayer online = ProxyServer.getInstance().getPlayer(uuid);
        if (online != null && online.getAddress() != null && online.getAddress().getAddress() != null) {
            String ip = PunishmentManager.normalizeIp(online.getAddress().getAddress().getHostAddress());
            cachePlayer(uuid, online.getName(), ip);
            return ip;
        }

        return ipCache.get(uuid);
    }
}
