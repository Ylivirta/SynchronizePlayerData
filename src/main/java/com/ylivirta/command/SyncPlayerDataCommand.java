package com.ylivirta.command;

import com.ylivirta.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.command.SimpleCommand;
import org.mineacademy.fo.plugin.SimplePlugin;

import java.util.ArrayList;
import java.util.List;

public class SyncPlayerDataCommand extends SimpleCommand {

	public SyncPlayerDataCommand() {
		super("SyncPlayerData|SPD");
		setPermission("SynchronizePlayerData.admin");
		setUsage("[reload]");
	}

	@Override
	protected void onCommand() {
		final String param = args.length > 0 ? args[0].toLowerCase() : "";

		if ("reload".equals(param))
		{
			try {
				Settings.Translation.clear();
				SimplePlugin.getInstance().reload();

				tell(Settings.Translation.RELOAD_SUCCESS);

			} catch (final Throwable error) {
				error.printStackTrace();
				tell(Settings.Translation.RELOAD_FAIL.replace("&key&", error.toString()));
			}
		} else {
			tell(Common.colorize("&eSynchronizePlayerData V1.0 made by Ylivirta"));
		}
	}

	@Override
	protected List<String> tabComplete() {
		if (args.length == 1)
			return completeLastWord("reload");

		return new ArrayList<>();
	}
}
