package trainboy888;

import net.md_5.bungee.api.ProxyServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckCommandTest extends CommandTestBase {
    private CheckCommand checkCommand;

    @Before
    public void setup() {
        super.setupBase();
        checkCommand = new CheckCommand(punishmentManager, messageConfig, nameResolver);

        when(messageConfig.getFormatted(eq("messages.usage-check"), anyMap())).thenReturn("Usage");
        when(messageConfig.getFormatted(eq("messages.check-none"), anyMap())).thenReturn("None");
        when(messageConfig.getFormatted(eq("messages.check-header"), anyMap())).thenReturn("Header");
        when(messageConfig.getFormatted(eq("messages.check-entry"), anyMap())).thenReturn("Entry");
    }

    @Test
    public void testCheckNoPermission() {
        when(sender.hasPermission("bungeejustice.check")).thenReturn(false);

        checkCommand.execute(sender, new String[]{TEST_PLAYER_NAME});

        assertMessageSent();
    }

    @Test
    public void testCheckUsageWhenMissingTarget() {
        checkCommand.execute(sender, new String[]{});

        assertMessageSent();
        verify(messageConfig).getFormatted(eq("messages.usage-check"), anyMap());
    }

    @Test
    public void testCheckInvalidTarget() {
        ProxyServer proxy = mock(ProxyServer.class);
        when(proxy.getPlayer("UnknownPlayer")).thenReturn(null);
        when(nameResolver.resolveUUID("UnknownPlayer")).thenReturn(null);

        try (MockedStatic<ProxyServer> mockedProxy = org.mockito.Mockito.mockStatic(ProxyServer.class)) {
            mockedProxy.when(ProxyServer::getInstance).thenReturn(proxy);
            checkCommand.execute(sender, new String[]{"UnknownPlayer"});
        }

        assertMessageSent();
        verify(messageConfig, never()).getFormatted(eq("messages.check-header"), anyMap());
    }

    @Test
    public void testCheckNoPunishments() {
        ProxyServer proxy = mock(ProxyServer.class);
        when(proxy.getPlayer(TEST_PLAYER_NAME)).thenReturn(null);

        when(punishmentManager.getActivePlayerBan(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerMute(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerWarn(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerNote(TEST_PLAYER_UUID)).thenReturn(null);

        try (MockedStatic<ProxyServer> mockedProxy = org.mockito.Mockito.mockStatic(ProxyServer.class)) {
            mockedProxy.when(ProxyServer::getInstance).thenReturn(proxy);
            checkCommand.execute(sender, new String[]{TEST_PLAYER_NAME});
        }

        verify(messageConfig).getFormatted(eq("messages.check-none"), anyMap());
        verify(sender, times(1)).sendMessage(org.mockito.ArgumentMatchers.any(net.md_5.bungee.api.chat.TextComponent.class));
    }

    @Test
    public void testCheckShowsAllPunishments() {
        ProxyServer proxy = mock(ProxyServer.class);
        when(proxy.getPlayer(TEST_PLAYER_NAME)).thenReturn(null);

        Punishment ban = new Punishment("1", PunishmentType.BAN, TEST_PLAYER_UUID.toString(), "Ban Reason", TEST_ADMIN_NAME, System.currentTimeMillis(), -1L);
        Punishment mute = new Punishment("2", PunishmentType.MUTE, TEST_PLAYER_UUID.toString(), "Mute Reason", TEST_ADMIN_NAME, System.currentTimeMillis(), -1L);
        Punishment warn = new Punishment("3", PunishmentType.WARN, TEST_PLAYER_UUID.toString(), "Warn Reason", TEST_ADMIN_NAME, System.currentTimeMillis(), -1L);
        Punishment note = new Punishment("4", PunishmentType.NOTE, TEST_PLAYER_UUID.toString(), "Note Reason", TEST_ADMIN_NAME, System.currentTimeMillis(), -1L);

        when(punishmentManager.getActivePlayerBan(TEST_PLAYER_UUID)).thenReturn(ban);
        when(punishmentManager.getActivePlayerMute(TEST_PLAYER_UUID)).thenReturn(mute);
        when(punishmentManager.getActivePlayerWarn(TEST_PLAYER_UUID)).thenReturn(warn);
        when(punishmentManager.getActivePlayerNote(TEST_PLAYER_UUID)).thenReturn(note);
        when(punishmentManager.getHistoricalPlayerPunishments(TEST_PLAYER_UUID, TEST_PLAYER_NAME)).thenReturn(new LinkedHashMap<>());

        try (MockedStatic<ProxyServer> mockedProxy = org.mockito.Mockito.mockStatic(ProxyServer.class)) {
            mockedProxy.when(ProxyServer::getInstance).thenReturn(proxy);
            checkCommand.execute(sender, new String[]{TEST_PLAYER_NAME});
        }

        ArgumentCaptor<Map<String, String>> headerMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messageConfig).getFormatted(eq("messages.check-header"), headerMapCaptor.capture());
        assertEquals("4", headerMapCaptor.getValue().get("count"));

        verify(messageConfig, times(4)).getFormatted(eq("messages.check-entry"), anyMap());
        verify(sender, times(5)).sendMessage(org.mockito.ArgumentMatchers.any(net.md_5.bungee.api.chat.TextComponent.class));
    }

    @Test
    public void testCheckIncludesHistoricalPunishments() {
        ProxyServer proxy = mock(ProxyServer.class);
        when(proxy.getPlayer(TEST_PLAYER_NAME)).thenReturn(null);

        when(punishmentManager.getActivePlayerBan(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerMute(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerWarn(TEST_PLAYER_UUID)).thenReturn(null);
        when(punishmentManager.getActivePlayerNote(TEST_PLAYER_UUID)).thenReturn(null);

        Map<String, PunishmentManager.HistoricalPunishment> history = new LinkedHashMap<>();
        history.put("9", new PunishmentManager.HistoricalPunishment(
                "9",
                PunishmentType.BAN,
                "Expired ban",
                TEST_ADMIN_NAME,
                System.currentTimeMillis() - 3600000,
                "Expired"
        ));
        when(punishmentManager.getHistoricalPlayerPunishments(TEST_PLAYER_UUID, TEST_PLAYER_NAME)).thenReturn(history);

        try (MockedStatic<ProxyServer> mockedProxy = org.mockito.Mockito.mockStatic(ProxyServer.class)) {
            mockedProxy.when(ProxyServer::getInstance).thenReturn(proxy);
            checkCommand.execute(sender, new String[]{TEST_PLAYER_NAME});
        }

        ArgumentCaptor<Map<String, String>> headerMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messageConfig).getFormatted(eq("messages.check-header"), headerMapCaptor.capture());
        assertEquals("1", headerMapCaptor.getValue().get("count"));
        verify(messageConfig, times(1)).getFormatted(eq("messages.check-entry"), anyMap());
    }
}
