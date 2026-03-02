package trainboy888;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {
    private final Plugin plugin;
    private final File file;
    private int nextId = 1;

    private final Map<UUID, Punishment> playerBans = new HashMap<>();
    private final Map<UUID, Punishment> playerMutes = new HashMap<>();
    private final Map<UUID, Punishment> playerKicks = new HashMap<>();
    private final Map<UUID, Punishment> playerWarns = new HashMap<>();
    private final Map<UUID, Punishment> playerNotes = new HashMap<>();
    private final Map<String, Punishment> ipBans = new HashMap<>();
    private final Map<String, Punishment> ipMutes = new HashMap<>();
    private final Map<String, Punishment> allPunishmentsById = new HashMap<>();

    public PunishmentManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "punishments.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }

        if (!file.exists()) {
            save();
            return;
        }

        try {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

            Configuration players = configuration.getSection("players");
            if (players != null) {
                for (String uuidKey : players.getKeys()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidKey);
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }

                    Configuration playerSection = players.getSection(uuidKey);
                    loadSingle(playerSection, "ban", playerBans, uuid);
                    loadSingle(playerSection, "mute", playerMutes, uuid);
                    loadSingle(playerSection, "kick", playerKicks, uuid);
                    loadSingle(playerSection, "warn", playerWarns, uuid);
                    loadSingle(playerSection, "note", playerNotes, uuid);
                }
            }

            Configuration ips = configuration.getSection("ips");
            if (ips != null) {
                for (String ip : ips.getKeys()) {
                    Configuration ipSection = ips.getSection(ip);
                    loadSingle(ipSection, "ban", ipBans, normalizeIp(ip));
                    loadSingle(ipSection, "mute", ipMutes, normalizeIp(ip));
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to load punishments.yml: " + exception.getMessage());
        }
    }

    private <T> void loadSingle(Configuration parent, String node, Map<T, Punishment> map, T key) {
        if (parent == null || !parent.contains(node + ".reason")) {
            return;
        }

        String id = parent.getString(node + ".id", generateId());
        String reason = parent.getString(node + ".reason", "No reason provided");
        String actor = parent.getString(node + ".actor", "CONSOLE");
        long createdAt = parent.getLong(node + ".createdAt", System.currentTimeMillis());
        long expiresAt = parent.getLong(node + ".expiresAt", -1L);

        PunishmentType type = determineType(node, map == ipBans || map == ipMutes);
        Punishment punishment = new Punishment(id, type, reason, actor, createdAt, expiresAt);

        if (!punishment.isExpired()) {
            map.put(key, punishment);
            allPunishmentsById.put(id, punishment);
        }
    }

    private PunishmentType determineType(String node, boolean ip) {
        if (ip) {
            return node.equals("ban") ? PunishmentType.IP_BAN : PunishmentType.IP_MUTE;
        }
        switch (node) {
            case "ban":
                return PunishmentType.BAN;
            case "mute":
                return PunishmentType.MUTE;
            case "kick":
                return PunishmentType.KICK;
            case "warn":
                return PunishmentType.WARN;
            case "note":
                return PunishmentType.NOTE;
            default:
                return PunishmentType.BAN;
        }
    }

    private String generateId() {
        return String.valueOf(nextId++);
    }

    public void save() {
        try {
            Configuration root = new Configuration();
            Configuration players = new Configuration();
            Configuration ips = new Configuration();

            for (Map.Entry<UUID, Punishment> entry : playerBans.entrySet()) {
                savePlayerNode(players, entry.getKey(), "ban", entry.getValue());
            }

            for (Map.Entry<UUID, Punishment> entry : playerMutes.entrySet()) {
                savePlayerNode(players, entry.getKey(), "mute", entry.getValue());
            }

            for (Map.Entry<UUID, Punishment> entry : playerKicks.entrySet()) {
                savePlayerNode(players, entry.getKey(), "kick", entry.getValue());
            }

            for (Map.Entry<UUID, Punishment> entry : playerWarns.entrySet()) {
                savePlayerNode(players, entry.getKey(), "warn", entry.getValue());
            }

            for (Map.Entry<UUID, Punishment> entry : playerNotes.entrySet()) {
                savePlayerNode(players, entry.getKey(), "note", entry.getValue());
            }

            for (Map.Entry<String, Punishment> entry : ipBans.entrySet()) {
                saveIpNode(ips, entry.getKey(), "ban", entry.getValue());
            }

            for (Map.Entry<String, Punishment> entry : ipMutes.entrySet()) {
                saveIpNode(ips, entry.getKey(), "mute", entry.getValue());
            }

            root.set("players", players);
            root.set("ips", ips);

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(root, file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save punishments.yml: " + exception.getMessage());
        }
    }

    private void savePlayerNode(Configuration players, UUID uuid, String node, Punishment punishment) {
        Configuration playerSection = players.getSection(uuid.toString());
        if (playerSection == null) {
            playerSection = new Configuration();
            players.set(uuid.toString(), playerSection);
        }
        writePunishment(playerSection, node, punishment);
    }

    private void saveIpNode(Configuration ips, String ip, String node, Punishment punishment) {
        Configuration ipSection = ips.getSection(ip);
        if (ipSection == null) {
            ipSection = new Configuration();
            ips.set(ip, ipSection);
        }
        writePunishment(ipSection, node, punishment);
    }

    private void writePunishment(Configuration section, String node, Punishment punishment) {
        section.set(node + ".id", punishment.getId());
        section.set(node + ".reason", punishment.getReason());
        section.set(node + ".actor", punishment.getActor());
        section.set(node + ".createdAt", punishment.getCreatedAt());
        section.set(node + ".expiresAt", punishment.getExpiresAt());
    }

    public void punishPlayer(UUID uuid, PunishmentType type, String reason, String actor, long durationMillis) {
        long expiresAt = durationMillis <= 0 ? -1L : System.currentTimeMillis() + durationMillis;
        String id = generateId();
        Punishment punishment = new Punishment(id, type, reason, actor, System.currentTimeMillis(), expiresAt);

        switch (type) {
            case BAN:
                playerBans.put(uuid, punishment);
                break;
            case MUTE:
                playerMutes.put(uuid, punishment);
                break;
            case KICK:
                playerKicks.put(uuid, punishment);
                break;
            case WARN:
                playerWarns.put(uuid, punishment);
                break;
            case NOTE:
                playerNotes.put(uuid, punishment);
                break;
        }

        allPunishmentsById.put(id, punishment);
        
        // Only save persistent punishments (not kicks)
        if (type != PunishmentType.KICK) {
            save();
        }
    }

    public void punishIp(String ip, PunishmentType type, String reason, String actor, long durationMillis) {
        long expiresAt = durationMillis <= 0 ? -1L : System.currentTimeMillis() + durationMillis;
        String id = generateId();
        Punishment punishment = new Punishment(id, type, reason, actor, System.currentTimeMillis(), expiresAt);
        String normalized = normalizeIp(ip);

        if (type == PunishmentType.IP_BAN) {
            ipBans.put(normalized, punishment);
        } else if (type == PunishmentType.IP_MUTE) {
            ipMutes.put(normalized, punishment);
        }

        allPunishmentsById.put(id, punishment);
        save();
    }

    public boolean unbanPlayer(UUID uuid) {
        boolean removed = playerBans.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean unmutePlayer(UUID uuid) {
        boolean removed = playerMutes.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean unbanIp(String ip) {
        boolean removed = ipBans.remove(normalizeIp(ip)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean unmuteIp(String ip) {
        boolean removed = ipMutes.remove(normalizeIp(ip)) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean removeById(String id) {
        Punishment punishment = allPunishmentsById.remove(id);
        if (punishment == null) {
            return false;
        }

        // Remove from appropriate map - this is tricky since we don't know the key
        // For now, we'll just remove from the ID map
        save();
        return true;
    }

    public Punishment getById(String id) {
        return allPunishmentsById.get(id);
    }

    public Map<String, Punishment> getAllPunishments() {
        purgeExpiredAll();
        return new LinkedHashMap<>(allPunishmentsById);
    }

    public Punishment getActivePlayerBan(UUID uuid) {
        return getActive(playerBans, uuid);
    }

    public Punishment getActivePlayerMute(UUID uuid) {
        return getActive(playerMutes, uuid);
    }

    public Punishment getActiveIpBan(String ip) {
        return getActive(ipBans, normalizeIp(ip));
    }

    public Punishment getActiveIpMute(String ip) {
        return getActive(ipMutes, normalizeIp(ip));
    }

    public Map<UUID, Punishment> getActivePlayerBans() {
        purgeExpired(playerBans);
        return new LinkedHashMap<>(playerBans);
    }

    public Map<String, Punishment> getActiveIpBans() {
        purgeExpired(ipBans);
        return new LinkedHashMap<>(ipBans);
    }

    private <T> Punishment getActive(Map<T, Punishment> map, T key) {
        Punishment punishment = map.get(key);
        if (punishment == null) {
            return null;
        }
        if (punishment.isExpired()) {
            map.remove(key);
            save();
            return null;
        }
        return punishment;
    }

    private <T> void purgeExpired(Map<T, Punishment> map) {
        boolean changed = false;
        Iterator<Map.Entry<T, Punishment>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<T, Punishment> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            save();
        }
    }

    private void purgeExpiredAll() {
        boolean changed = false;
        Iterator<Map.Entry<String, Punishment>> iterator = allPunishmentsById.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Punishment> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            save();
        }
    }

    public static String normalizeIp(String ip) {
        return ip.trim().toLowerCase();
    }
}
