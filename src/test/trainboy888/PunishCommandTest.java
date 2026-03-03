package trainboy888;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PunishCommand (ban, tempban, mute, tempmute, ipban, etc.)
 */
public class PunishCommandTest extends CommandTestBase {
    private PunishCommand banCommand;
    private PunishCommand tempbanCommand;
    private PunishCommand muteCommand;

    @Before
    public void setup() {
        super.setupBase();
        banCommand = new PunishCommand("ban", "bungeejustice.ban", punishmentManager, messageConfig, PunishmentType.BAN, false, false);
        tempbanCommand = new PunishCommand("tempban", "bungeejustice.tempban", punishmentManager, messageConfig, PunishmentType.BAN, true, false);
        muteCommand = new PunishCommand("mute", "bungeejustice.mute", punishmentManager, messageConfig, PunishmentType.MUTE, false, false);
    }

    @Test
    public void testBanCommandNoPermission() {
        when(sender.hasPermission("bungeejustice.ban")).thenReturn(false);

        String[] args = {TEST_PLAYER_NAME};
        banCommand.execute(sender, args);

        assertMessageSent();
    }

    @Test
    public void testBanCommandInsufficientArgs() {
        String[] args = {};
        banCommand.execute(sender, args);

        assertMessageSent();
    }

    @Test
    public void testBanCommandSuccess() {
        String[] args = {TEST_PLAYER_NAME, "Cheating"};
        banCommand.execute(sender, args);

        assertPunishmentApplied(PunishmentType.BAN);
        assertMessageSent();
    }

    @Test
    public void testTempbanCommandInvalidDuration() {
        when(messageConfig.get("messages.invalid-duration")).thenReturn("&cInvalid duration");
        String[] args = {TEST_PLAYER_NAME, "invalid_duration", "Cheating"};
        tempbanCommand.execute(sender, args);

        assertMessageSent();
        verify(punishmentManager, never()).punishPlayer(any(), any(), anyString(), anyString(), anyLong());
    }

    @Test
    public void testTempbanCommandValidDuration() {
        String[] args = {TEST_PLAYER_NAME, "30m", "Spam"};
        tempbanCommand.execute(sender, args);

        assertPunishmentApplied(PunishmentType.BAN);
        assertMessageSent();
    }

    @Test
    public void testMuteCommandSuccess() {
        String[] args = {TEST_PLAYER_NAME, "Spam"};
        muteCommand.execute(sender, args);

        assertPunishmentApplied(PunishmentType.MUTE);
        assertMessageSent();
    }

    @Test
    public void testBanCommandWithDefaultReason() {
        when(messageConfig.get("messages.default-reason")).thenReturn("No reason provided");
        String[] args = {TEST_PLAYER_NAME};
        banCommand.execute(sender, args);

        verify(punishmentManager).punishPlayer(
                any(java.util.UUID.class),
                eq(PunishmentType.BAN),
                argThat(reason -> reason.contains("No reason") || reason.isEmpty()),
                anyString(),
                anyLong()
        );
    }
}
