package trainboy888;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for BanListCommand permission and listing behavior
 */
public class BanListCommandTest extends CommandTestBase {
    private BanListCommand banListCommand;

    @Before
    public void setup() {
        super.setupBase();
        banListCommand = new BanListCommand(punishmentManager, messageConfig, nameResolver);

        when(messageConfig.get("messages.punishlist-empty")).thenReturn("No punishments.");
        when(messageConfig.getFormatted(eq("messages.punishlist-header"), anyMap())).thenReturn("Header");
        when(messageConfig.getFormatted(eq("messages.punishlist-entry"), anyMap())).thenReturn("Entry");
    }

    @Test
    public void testBanListNoPermission() {
        when(sender.hasPermission("bungeejustice.banlist")).thenReturn(false);

        banListCommand.execute(sender, new String[]{});

        assertMessageSent();
        verify(punishmentManager, never()).getAllPunishments();
    }

    @Test
    public void testBanListExcludesKicksFromCountAndEntries() {
        UUID kickUuid = UUID.randomUUID();
        UUID banUuid = UUID.randomUUID();

        Punishment kick = new Punishment("1", PunishmentType.KICK, kickUuid.toString(), "Kick reason", "Admin", System.currentTimeMillis(), -1L);
        Punishment ban = new Punishment("2", PunishmentType.BAN, banUuid.toString(), "Ban reason", "Admin", System.currentTimeMillis(), -1L);

        Map<String, Punishment> punishments = new LinkedHashMap<>();
        punishments.put(kick.getId(), kick);
        punishments.put(ban.getId(), ban);

        when(punishmentManager.getAllPunishments()).thenReturn(punishments);
        when(nameResolver.resolveName(kickUuid)).thenReturn("KickPlayer");
        when(nameResolver.resolveName(banUuid)).thenReturn("BanPlayer");

        banListCommand.execute(sender, new String[]{});

        ArgumentCaptor<Map<String, String>> headerMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messageConfig).getFormatted(eq("messages.punishlist-header"), headerMapCaptor.capture());
        assertEquals("1", headerMapCaptor.getValue().get("count"));

        verify(messageConfig, times(1)).getFormatted(eq("messages.punishlist-entry"), anyMap());

        ArgumentCaptor<TextComponent> messageCaptor = ArgumentCaptor.forClass(TextComponent.class);
        verify(sender, times(2)).sendMessage(messageCaptor.capture());
        List<TextComponent> sentMessages = messageCaptor.getAllValues();

        TextComponent entryMessage = sentMessages.get(1);
        assertEquals("/banlist 2", entryMessage.getClickEvent().getValue());
        assertEquals("Click to view details for punishment #2", entryMessage.getHoverEvent().getValue().get(0).toPlainText());
    }
}
