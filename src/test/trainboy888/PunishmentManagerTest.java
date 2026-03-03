package trainboy888;

import net.md_5.bungee.api.plugin.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests for PunishmentManager core functionality
 */
public class PunishmentManagerTest {
    @Mock
    private Plugin plugin;

    @Mock
    private Logger logger;

    private PunishmentManager manager;
    private final UUID testUUID = UUID.randomUUID();
    private final String testIP = "192.168.1.100";

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Mock plugin methods
        when(plugin.getDataFolder()).thenReturn(new File("./test-data"));
        when(plugin.getLogger()).thenReturn(logger);
        
        manager = new PunishmentManager(plugin);
    }

    @Test
    public void testPunishPlayerBan() {
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Test Ban", "Admin", -1L);

        Punishment punishment = manager.getActivePlayerBan(testUUID);
        assertNotNull(punishment);
        assertEquals(PunishmentType.BAN, punishment.getType());
        assertTrue(punishment.isPermanent());
    }

    @Test
    public void testPunishPlayerTemporaryBan() {
        long duration = 60000; // 1 minute
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Temp Ban", "Admin", duration);

        Punishment punishment = manager.getActivePlayerBan(testUUID);
        assertNotNull(punishment);
        assertFalse(punishment.isPermanent());
        assertTrue(punishment.getExpiresAt() > System.currentTimeMillis());
    }

    @Test
    public void testPunishPlayerMute() {
        manager.punishPlayer(testUUID, PunishmentType.MUTE, "Spam", "Admin", -1L);

        Punishment punishment = manager.getActivePlayerMute(testUUID);
        assertNotNull(punishment);
        assertEquals(PunishmentType.MUTE, punishment.getType());
    }

    @Test
    public void testUnbanPlayer() {
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Ban", "Admin", -1L);
        boolean removed = manager.unbanPlayer(testUUID);

        assertTrue(removed);
        assertNull(manager.getActivePlayerBan(testUUID));
    }

    @Test
    public void testUnbanPlayerNonExistent() {
        boolean removed = manager.unbanPlayer(UUID.randomUUID());
        assertFalse(removed);
    }

    @Test
    public void testUnmutePlayer() {
        manager.punishPlayer(testUUID, PunishmentType.MUTE, "Spam", "Admin", -1L);
        boolean removed = manager.unmutePlayer(testUUID);

        assertTrue(removed);
        assertNull(manager.getActivePlayerMute(testUUID));
    }

    @Test
    public void testPunishIP() {
        manager.punishIp(testIP, PunishmentType.IP_BAN, "IP Ban", "Admin", -1L);

        Punishment punishment = manager.getActiveIpBan(testIP);
        assertNotNull(punishment);
        assertEquals(PunishmentType.IP_BAN, punishment.getType());
    }

    @Test
    public void testUnbanIP() {
        manager.punishIp(testIP, PunishmentType.IP_BAN, "IP Ban", "Admin", -1L);
        boolean removed = manager.unbanIp(testIP);

        assertTrue(removed);
        assertNull(manager.getActiveIpBan(testIP));
    }

    @Test
    public void testGetPunishmentById() {
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Ban", "Admin", -1L);
        
        // Get all punishments to find the ID
        var allPunishments = manager.getAllPunishments();
        assertFalse(allPunishments.isEmpty());
        
        String punishmentId = allPunishments.keySet().iterator().next();
        Punishment punishment = manager.getById(punishmentId);
        
        assertNotNull(punishment);
        assertEquals(PunishmentType.BAN, punishment.getType());
    }

    @Test
    public void testRemoveById() {
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Ban", "Admin", -1L);
        
        var allPunishments = manager.getAllPunishments();
        String punishmentId = allPunishments.keySet().iterator().next();
        
        boolean removed = manager.removeById(punishmentId);
        assertTrue(removed);
        assertNull(manager.getById(punishmentId));
    }

    @Test
    public void testGetAllPunishments() {
        manager.punishPlayer(testUUID, PunishmentType.BAN, "Ban", "Admin", -1L);
        manager.punishIp(testIP, PunishmentType.IP_BAN, "IP Ban", "Admin", -1L);

        var allPunishments = manager.getAllPunishments();
        assertEquals(2, allPunishments.size());
    }

    @Test
    public void testMultiplePunishmentsOnPlayer() {
        UUID uuid = UUID.randomUUID();
        manager.punishPlayer(uuid, PunishmentType.BAN, "Ban", "Admin", -1L);
        manager.punishPlayer(uuid, PunishmentType.WARN, "Warning", "Admin", -1L);

        assertNotNull(manager.getActivePlayerBan(uuid));
        assertNotNull(manager.getActivePlayerMute(uuid)); // This should be null, ban is active
    }

    @Test
    public void testNormalizeIP() {
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.1 ";
        String ip3 = "192.168.1.1\t";

        assertEquals(PunishmentManager.normalizeIp(ip1), PunishmentManager.normalizeIp(ip2));
        assertEquals(PunishmentManager.normalizeIp(ip1), PunishmentManager.normalizeIp(ip3));
    }
}
