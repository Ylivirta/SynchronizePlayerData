package com.ylivirta.listeners;

import com.ylivirta.mysql.impl.PlayerDatabase;
import com.ylivirta.settings.Settings;
import com.ylivirta.util.Any;
import com.ylivirta.util.SetExpFix;
import com.ylivirta.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.remain.Remain;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class PlayerListener implements Listener {

	private final static PlayerDatabase PLAYER_DATABASE = PlayerDatabase.getInstance();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreLogin(final AsyncPlayerPreLoginEvent event)
	{
		AsyncPlayerPreLoginEvent.Result result = event.getLoginResult();

		if (PLAYER_DATABASE.isLoaded() &&
				result == AsyncPlayerPreLoginEvent.Result.ALLOWED)
		{
			final UUID uid = event.getUniqueId();

			if (PLAYER_DATABASE.hasAccount(uid))
			{
				PLAYER_DATABASE.deleteSync(uid);
				Common.runLaterAsync(Settings.MySQL.MYSQL_DELAY / 1000 * 20, () ->
				{
					ConcurrentMap<String, Any<?>> values = PLAYER_DATABASE.getData(uid);

					Valid.checkNotNull(values,Settings.Translation
							.getLangKeyReplace("is_null","Line 46 values")
					);

					final double health = values.get("health").asDouble();
					final float saturation = values.get("saturation").asFloat();
					final int foodLevel = values.get("food_level").asInteger();
					final int totalExp = values.get("total_exp").asInteger();
					final String endChest = values.get("end_chest").asString();
					final String inv = values.get("inventory").asString();
					final String armor = values.get("armor").asString();

					Common.runLater(() ->
					{
						final Player player = Remain.getPlayerByUUID(uid);

						if (player == null)
							return;

						player.setHealth(health);
						player.setSaturation(saturation);
						player.setFoodLevel(foodLevel);
						SetExpFix.setTotalExperience(player, totalExp);
						Objects.requireNonNull(player.getEquipment()).setArmorContents(Util.decodeItems(armor));
						player.getInventory().setContents(Util.decodeItems(inv));
						player.getEnderChest().setContents(Util.decodeItems(endChest));

						PLAYER_DATABASE.completeSync(uid);
					});
				});
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uid = player.getUniqueId();

		if (PLAYER_DATABASE.isLoaded() && !PLAYER_DATABASE.isSyncComplete(uid)) {
			Common.runLaterAsync(20, () -> {
				if (!PLAYER_DATABASE.hasAccount(uid))
					PLAYER_DATABASE.createAccount(uid, player);

				PLAYER_DATABASE.successfulJoinAdd(uid);
			});
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final UUID uid = player.getUniqueId();

		if (PLAYER_DATABASE.isLoaded() &&
				PLAYER_DATABASE.successfulJoinHas(uid)) {

			PLAYER_DATABASE.deleteSync(uid);
			Common.runLaterAsync(1, () -> {
				final double healthScale = player.getHealthScale();
				final double health = player.getHealth();
				final float saturation = player.getSaturation();
				final int foodLevel = player.getFoodLevel();
				final int totalExp = SetExpFix.getTotalExperience(player);
				final Inventory endChest = player.getEnderChest();
				final Inventory inv = player.getInventory();
				EntityEquipment armor = player.getEquipment();

				PLAYER_DATABASE.successfulJoinDelete(uid);
				PLAYER_DATABASE.setData(uid, player,
						health, healthScale,
						foodLevel, saturation,
						totalExp,
						Util.encodeItems(endChest.getContents()),
						Util.encodeItems(inv.getContents()),
						Util.encodeItems(armor.getArmorContents())
				);

				if (Debugger.isDebugModeEnabled()) {
					Common.log(Settings.Translation.getLang("synced"));
				}
			});
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();

		if (!PLAYER_DATABASE.isSyncComplete(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();

		if (!PLAYER_DATABASE.isSyncComplete(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}
}
