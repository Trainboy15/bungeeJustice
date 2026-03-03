package trainboy888;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for simple commands: Kick, Warn, Note (via PunishCommand)
 */
public class SimpleCommandsTest extends CommandTestBase {
    private PunishCommand kickCommand;
    private PunishCommand warnCommand;
    private PunishCommand noteCommand;

    @Before
    public void setup() {
        super.setupBase();
        kickCommand = new PunishCommand("kick", "bungeejustice.kick", punishmentManager, messageConfig, nameResolver, PunishmentType.KICK);
        warnCommand = new PunishCommand("warn", "bungeejustice.warn", punishmentManager, messageConfig, nameResolver, PunishmentType.WARN);
        noteCommand = new PunishCommand("note", "bungeejustice.note", punishmentManager, messageConfig, nameResolver, PunishmentType.NOTE);

        when(messageConfig.get("screens.kick")).thenReturn("&cYou were kicked");
        when(messageConfig.get("screens.warn")).thenReturn("&eYou were warned");
    }

    @Test
    public void testKickCommandNoPermission() {
        when(sender.hasPermission("bungeejustice.kick")).thenReturn(false);

        String[] args = {TEST_PLAYER_NAME};
        kickCommand.execute(sender, args);

        assertMessageSent();
    }

    @Test
    public void testKickCommandPlayerNotOnline() {
        try (MockedStatic<ProxyServer> mockedProxy = mockStatic(ProxyServer.class)) {
            mockedProxy.when(ProxyServer::getInstance).thenReturn(mock(ProxyServer.class));

            String[] args = {TEST_PLAYER_NAME};
            kickCommand.execute(sender, args);

            assertMessageSent();
            verify(punishmentManager, never()).punishPlayer(any(), any(), anyString(), anyString(), anyLong());
        }
    }

    @Test
    public void testWarnCommandSuccess() {
        String[] args = {TEST_PLAYER_NAME, "Spam"};
        warnCommand.execute(sender, args);

        verify(punishmentManager).punishPlayer(
                eq(TEST_PLAYER_UUID),
                eq(PunishmentType.WARN),
                argThat(r -> r.contains("Spam")),
                eq(TEST_ADMIN_NAME),
                eq(-1L)
        );
        assertMessageSent();
    }

    @Test
    public void testWarnCommandInsufficientArgs() {
        String[] args = {};
        warnCommand.execute(sender, args);

        assertMessageSent();
        verify(punishmentManager, never()).punishPlayer(any(), any(), anyString(), anyString(), anyLong());
    }

    @Test
    public void testNoteCommandSuccess() {
        String[] args = {TEST_PLAYER_NAME, "User", "is", "suspicious"};
        noteCommand.execute(sender, args);

        verify(punishmentManager).punishPlayer(
                eq(TEST_PLAYER_UUID),
                eq(PunishmentType.NOTE),
                argThat(r -> r.contains("suspicious")),
                eq(TEST_ADMIN_NAME),
                eq(-1L)
        );
        assertMessageSent();
    }

    @Test
    public void testNoteCommandDefaultReason() {
        when(messageConfig.get("messages.default-reason")).thenReturn("No reason provided");

        String[] args = {TEST_PLAYER_NAME};
        noteCommand.execute(sender, args);

        verify(punishmentManager).punishPlayer(
                eq(TEST_PLAYER_UUID),
                eq(PunishmentType.NOTE),
                anyString(),
                eq(TEST_ADMIN_NAME),
                eq(-1L)
        );
        assertMessageSent();
    }
}
