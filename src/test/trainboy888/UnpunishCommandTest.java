package trainboy888;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for UnpunishCommand (remove by ID)
 */
public class UnpunishCommandTest extends CommandTestBase {
    private UnpunishCommand unpunishCommand;
    private Punishment mockPunishment;

    @Before
    public void setup() {
        super.setupBase();
        unpunishCommand = new UnpunishCommand(punishmentManager, messageConfig);
        mockPunishment = new Punishment("1", PunishmentType.BAN, TEST_PLAYER_UUID.toString(), TEST_REASON, TEST_ADMIN_NAME, System.currentTimeMillis(), -1L);
    }

    @Test
    public void testUnpunishNoPermission() {
        when(sender.hasPermission("bungeejustice.unpunish")).thenReturn(false);

        String[] args = {"1"};
        unpunishCommand.execute(sender, args);

        assertMessageSent();
    }

    @Test
    public void testUnpunishInsufficientArgs() {
        String[] args = {};
        unpunishCommand.execute(sender, args);

        assertMessageSent();
        verify(punishmentManager, never()).removeById(anyString());
    }

    @Test
    public void testUnpunishPunishmentNotFound() {
        when(punishmentManager.getById("999")).thenReturn(null);

        String[] args = {"999"};
        unpunishCommand.execute(sender, args);

        assertMessageSent();
        verify(punishmentManager, never()).removeById("999");
    }

    @Test
    public void testUnpunishSuccess() {
        when(punishmentManager.getById("1")).thenReturn(mockPunishment);
        when(punishmentManager.removeById("1")).thenReturn(true);

        String[] args = {"1"};
        unpunishCommand.execute(sender, args);

        verify(punishmentManager).removeById("1");
        assertMessageSent();
    }

    @Test
    public void testUnpunishRemovalFailed() {
        when(punishmentManager.getById("1")).thenReturn(mockPunishment);
        when(punishmentManager.removeById("1")).thenReturn(false);

        String[] args = {"1"};
        unpunishCommand.execute(sender, args);

        verify(punishmentManager).removeById("1");
        assertMessageSent();
    }
}
