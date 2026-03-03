package trainboy888;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests for Punishment class
 */
public class PunishmentTest {
    private static final String TEST_ID = "1";
    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String TEST_UUID_STR = TEST_UUID.toString();
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_REASON = "Test Reason";
    private static final String TEST_ACTOR = "Admin";

    @Test
    public void testPermanentPunishment() {
        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.BAN,
                TEST_UUID_STR,
                TEST_REASON,
                TEST_ACTOR,
                System.currentTimeMillis(),
                -1L
        );

        assertTrue(punishment.isPermanent());
        assertFalse(punishment.isExpired());
    }

    @Test
    public void testTemporaryPunishmentNotExpired() {
        long futureTime = System.currentTimeMillis() + 60000; // 1 minute in future
        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.BAN,
                TEST_UUID_STR,
                TEST_REASON,
                TEST_ACTOR,
                System.currentTimeMillis(),
                futureTime
        );

        assertFalse(punishment.isPermanent());
        assertFalse(punishment.isExpired());
    }

    @Test
    public void testTemporaryPunishmentExpired() {
        long pastTime = System.currentTimeMillis() - 60000; // 1 minute in past
        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.BAN,
                TEST_UUID_STR,
                TEST_REASON,
                TEST_ACTOR,
                System.currentTimeMillis() - 120000,
                pastTime
        );

        assertFalse(punishment.isPermanent());
        assertTrue(punishment.isExpired());
    }

    @Test
    public void testGetPlayerUUID() {
        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.BAN,
                TEST_UUID_STR,
                TEST_REASON,
                TEST_ACTOR,
                System.currentTimeMillis(),
                -1L
        );

        assertEquals(TEST_UUID, punishment.getPlayerUUID());
    }

    @Test
    public void testGetIpAddress() {
        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.IP_BAN,
                TEST_IP,
                TEST_REASON,
                TEST_ACTOR,
                System.currentTimeMillis(),
                -1L
        );

        assertEquals(TEST_IP, punishment.getIpAddress());
        assertNull(punishment.getPlayerUUID());
    }

    @Test
    public void testGetters() {
        long createdAt = System.currentTimeMillis();
        long expiresAt = createdAt + 60000;

        Punishment punishment = new Punishment(
                TEST_ID,
                PunishmentType.MUTE,
                TEST_UUID_STR,
                TEST_REASON,
                TEST_ACTOR,
                createdAt,
                expiresAt
        );

        assertEquals(TEST_ID, punishment.getId());
        assertEquals(PunishmentType.MUTE, punishment.getType());
        assertEquals(TEST_REASON, punishment.getReason());
        assertEquals(TEST_ACTOR, punishment.getActor());
        assertEquals(createdAt, punishment.getCreatedAt());
        assertEquals(expiresAt, punishment.getExpiresAt());
        assertEquals(TEST_UUID_STR, punishment.getTarget());
    }

    @Test
    public void testDurationParsing() {
        // Test with various duration formats
        long thirtyMinutes = DurationParser.parseDurationMillis("30m");
        long oneHour = DurationParser.parseDurationMillis("1h");
        long oneDay = DurationParser.parseDurationMillis("1d");
        long twoWeeks = DurationParser.parseDurationMillis("2w");

        assertTrue(thirtyMinutes > 0);
        assertTrue(oneHour > thirtyMinutes);
        assertTrue(oneDay > oneHour);
        assertTrue(twoWeeks > oneDay);
    }
}
