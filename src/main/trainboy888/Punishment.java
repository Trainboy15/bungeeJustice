package trainboy888;

public class Punishment {
    private final String id;
    private final PunishmentType type;
    private final String reason;
    private final String actor;
    private final long createdAt;
    private final long expiresAt;

    public Punishment(String id, PunishmentType type, String reason, String actor, long createdAt, long expiresAt) {
        this.id = id;
        this.type = type;
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
