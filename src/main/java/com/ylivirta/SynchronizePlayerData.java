package com.ylivirta;

import com.ylivirta.command.SyncPlayerDataCommand;
import com.ylivirta.listeners.PlayerListener;
import com.ylivirta.mysql.KeepAlive;
import com.ylivirta.mysql.impl.PlayerDatabase;
import com.ylivirta.settings.Settings;
import org.bukkit.event.HandlerList;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.settings.YamlStaticConfig;

import java.util.Collections;
import java.util.List;

public class SynchronizePlayerData extends SimplePlugin {

	private final static PlayerDatabase PLAYER_DATABASE = PlayerDatabase.getInstance();
	private PlayerListener playerListener;

	@Override
	protected void onPluginStart()
	{
		Common.runAsync(() ->
		{
			PLAYER_DATABASE.connect(
				Settings.MySQL.MYSQL_HOST,
			 	Settings.MySQL.MYSQL_PORT,
				Settings.MySQL.MYSQL_DATABASE,
				Settings.MySQL.MYSQL_USERNAME,
				Settings.MySQL.MYSQL_PASSWORD,
				Settings.MySQL.MYSQL_TABLE,
				true
			);
			PLAYER_DATABASE.setup();
		});

		registerCommand(new SyncPlayerDataCommand());

		playerListener = new PlayerListener();
		registerEvents(playerListener);

		Common.log("Enabled SynchronizePlayerData");
	}

	@Override
	protected void onPluginStop() {
		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(playerListener);
		PLAYER_DATABASE.onStop();
		PLAYER_DATABASE.closeConnection();
		Common.log("Disabled SynchronizePlayerData");
	}

	@Override
	protected void onReloadablesStart() {
		Common.runTimerAsync(1200, new KeepAlive());
	}

	@Override
	public List<Class<? extends YamlStaticConfig>> getSettings() {
		return Collections.singletonList(Settings.class);
	}
}
