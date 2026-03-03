package trainboy888;

import java.util.UUID;

public class Punishment {
    private final String id;
    private final PunishmentType type;
    private final String reason;
    private final String actor;
    private final long createdAt;
    private final long expiresAt;
    private final String target; // UUID string or IP address

    public Punishment(String id, PunishmentType type, String target, String reason, String actor, long createdAt, long expiresAt) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.reason = reason;
        this.actor = actor;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public PunishmentType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public UUID getPlayerUUID() {
        try {
            return UUID.fromString(target);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getIpAddress() {
        // If it's not a valid UUID, assume it's an IP
        if (getPlayerUUID() == null) {
            return target;
        }
        return null;
    }

    public String getReason() {
        return reason;
    }

    public String getActor() {
        return actor;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isPermanent() {
        return expiresAt <= 0;
    }

    public boolean isExpired() {
        return !isPermanent() && System.currentTimeMillis() > expiresAt;
    }
}
