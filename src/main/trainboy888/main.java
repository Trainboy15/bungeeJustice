package trainboy888;

import net.md_5.bungee.api.plugin.Plugin;

public class main extends Plugin {
    private PunishmentManager punishmentManager;
    private MessageConfig messageConfig;

    @Override
    public void onEnable() {
	punishmentManager = new PunishmentManager(this);
	punishmentManager.load();
	messageConfig = new MessageConfig(this);
	messageConfig.load();

	getProxy().getPluginManager().registerListener(this, new PunishmentListener(punishmentManager, messageConfig));

	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ban", "bungeejustice.ban", punishmentManager, messageConfig, PunishmentType.BAN, false, false));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempban", "bungeejustice.tempban", punishmentManager, messageConfig, PunishmentType.BAN, true, false));
	getProxy().getPluginManager().registerCommand(this,
		new UnpunishCommand("unban", "bungeejustice.unban", punishmentManager, messageConfig, PunishmentType.BAN, false));

	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("mute", "bungeejustice.mute", punishmentManager, messageConfig, PunishmentType.MUTE, false, false));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempmute", "bungeejustice.tempmute", punishmentManager, messageConfig, PunishmentType.MUTE, true, false));
	getProxy().getPluginManager().registerCommand(this,
		new UnpunishCommand("unmute", "bungeejustice.unmute", punishmentManager, messageConfig, PunishmentType.MUTE, false));

	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ipban", "bungeejustice.ipban", punishmentManager, messageConfig, PunishmentType.IP_BAN, false, true));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempipban", "bungeejustice.tempipban", punishmentManager, messageConfig, PunishmentType.IP_BAN, true, true));
	getProxy().getPluginManager().registerCommand(this,
		new UnpunishCommand("unipban", "bungeejustice.unipban", punishmentManager, messageConfig, PunishmentType.IP_BAN, true));

	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("ipmute", "bungeejustice.ipmute", punishmentManager, messageConfig, PunishmentType.IP_MUTE, false, true));
	getProxy().getPluginManager().registerCommand(this,
		new PunishCommand("tempipmute", "bungeejustice.tempipmute", punishmentManager, messageConfig, PunishmentType.IP_MUTE, true, true));
	getProxy().getPluginManager().registerCommand(this,
		new UnpunishCommand("unipmute", "bungeejustice.unipmute", punishmentManager, messageConfig, PunishmentType.IP_MUTE, true));
	getProxy().getPluginManager().registerCommand(this,
		new BanListCommand(punishmentManager, messageConfig));
	getProxy().getPluginManager().registerCommand(this,
		new BungeeJusticeCommand(messageConfig));

	getLogger().info("bungeeJustice enabled.");
    }

    @Override
    public void onDisable() {
	if (punishmentManager != null) {
	    punishmentManager.save();
	}
    }
}
