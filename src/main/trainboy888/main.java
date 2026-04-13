package trainboy888;

import net.md_5.bungee.api.plugin.Plugin;

public class main extends Plugin {
    private PunishmentManager punishmentManager;
    private MessageConfig messageConfig;
    private OfflineNameResolver nameResolver;
	private AuditLogger auditLogger;

    @Override
    public void onEnable() {
	nameResolver = new OfflineNameResolver(this);
	auditLogger = new AuditLogger(this, nameResolver);
	punishmentManager = new PunishmentManager(this, auditLogger);
	punishmentManager.load();
	messageConfig = new MessageConfig(this);
	messageConfig.load();

	getProxy().getPluginManager().registerListener(this, new PunishmentListener(punishmentManager, messageConfig, nameResolver));

	// Ban commands
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ban", "bungeejustice.ban", punishmentManager, messageConfig, nameResolver, PunishmentType.BAN, false, false));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempban", "bungeejustice.tempban", punishmentManager, messageConfig, nameResolver, PunishmentType.BAN, true, false));

	// Mute commands
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("mute", "bungeejustice.mute", punishmentManager, messageConfig, nameResolver, PunishmentType.MUTE, false, false));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempmute", "bungeejustice.tempmute", punishmentManager, messageConfig, nameResolver, PunishmentType.MUTE, true, false));

	// IP Ban commands
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ipban", "bungeejustice.ipban", punishmentManager, messageConfig, nameResolver, PunishmentType.IP_BAN, false, true));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempipban", "bungeejustice.tempipban", punishmentManager, messageConfig, nameResolver, PunishmentType.IP_BAN, true, true));

	// IP Mute commands
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ipmute", "bungeejustice.ipmute", punishmentManager, messageConfig, nameResolver, PunishmentType.IP_MUTE, false, true));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempipmute", "bungeejustice.tempipmute", punishmentManager, messageConfig, nameResolver, PunishmentType.IP_MUTE, true, true));

	// Simple punishment commands (kick, warn, note)
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("kick", "bungeejustice.kick", punishmentManager, messageConfig, nameResolver, PunishmentType.KICK));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("warn", "bungeejustice.warn", punishmentManager, messageConfig, nameResolver, PunishmentType.WARN));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("note", "bungeejustice.note", punishmentManager, messageConfig, nameResolver, PunishmentType.NOTE));

	// Single unified unpunish command
	getProxy().getPluginManager().registerCommand(this,
		new UnpunishCommand(punishmentManager, messageConfig));

	// Utility commands
	getProxy().getPluginManager().registerCommand(this,
		new BanListCommand(punishmentManager, messageConfig, nameResolver));
	getProxy().getPluginManager().registerCommand(this,
		new CheckCommand(punishmentManager, messageConfig, nameResolver));
	getProxy().getPluginManager().registerCommand(this,
		new BungeeJusticeCommand(this, messageConfig, punishmentManager));

	getLogger().info("bungeeJustice enabled.");
    }

    @Override
    public void onDisable() {
	if (punishmentManager != null) {
	    punishmentManager.save();
	}
	if (nameResolver != null) {
	    nameResolver.save();
	}
    }
}
