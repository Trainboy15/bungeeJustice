package trainboy888;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Base test class with common mocking setup for all command tests
 */
public class CommandTestBase {
    @Mock
    protected CommandSender sender;

    @Mock
    protected PunishmentManager punishmentManager;

    @Mock
    protected MessageConfig messageConfig;

    @Mock
    protected OfflineNameResolver nameResolver;

    @Mock
    protected ProxiedPlayer mockPlayer;

    protected static final UUID TEST_PLAYER_UUID = UUID.randomUUID();
    protected static final String TEST_PLAYER_NAME = "TestPlayer";
    protected static final String TEST_ADMIN_NAME = "Admin";
    protected static final String TEST_REASON = "Test Reason";
    protected static final String TEST_IP = "192.168.1.1";

    @Before
    public void setupBase() {
        MockitoAnnotations.openMocks(this);

        // Setup default sender (admin with all permissions)
        when(sender.getName()).thenReturn(TEST_ADMIN_NAME);
        when(sender.hasPermission("bungeejustice.ban")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.tempban")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.mute")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.tempmute")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.ipban")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.ipmute")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.kick")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.warn")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.note")).thenReturn(true);
        when(sender.hasPermission("bungeejustice.unpunish")).thenReturn(true);

        // Setup message config mocks
        when(messageConfig.get("messages.no-permission")).thenReturn("&cNo permission.");
        when(messageConfig.get("messages.invalid-duration")).thenReturn("&cInvalid duration.");
        when(messageConfig.get("messages.invalid-target-player")).thenReturn("&cTarget must be an online player or UUID.");
        when(messageConfig.get("messages.invalid-target-ip")).thenReturn("&cTarget must be an online player or valid IP.");
        when(messageConfig.get("messages.player-not-online")).thenReturn("&cPlayer not online.");
        when(messageConfig.get("messages.default-reason")).thenReturn("No reason provided");

        // Setup conditional formatting (for message templates)
        when(messageConfig.getFormatted("messages.applied-player", org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn("&aApplied punishment.");

        when(messageConfig.get("screens.kick"))
                .thenReturn("&cKicked from the server.");

        // Setup name resolver
        when(nameResolver.resolveName(TEST_PLAYER_UUID)).thenReturn(TEST_PLAYER_NAME);
        when(nameResolver.resolveUUID(TEST_PLAYER_NAME)).thenReturn(TEST_PLAYER_UUID);

        // Setup mock player
        when(mockPlayer.getUniqueId()).thenReturn(TEST_PLAYER_UUID);
        when(mockPlayer.getName()).thenReturn(TEST_PLAYER_NAME);
    }

    /**
     * Verify that a message was sent to the sender
     */
    protected void assertMessageSent() {
        org.mockito.Mockito.verify(sender).sendMessage(org.mockito.ArgumentMatchers.any(TextComponent.class));
    }

    /**
     * Verify that a specific punishment was created
     */
    protected void assertPunishmentApplied(PunishmentType type) {
        org.mockito.Mockito.verify(punishmentManager).punishPlayer(
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.eq(type),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }
}
