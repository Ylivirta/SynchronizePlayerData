package com.ylivirta.mysql.impl;

import com.ylivirta.mysql.model.ModelDatabase;
import com.ylivirta.settings.Settings;
import com.ylivirta.util.Any;
import com.ylivirta.util.SetExpFix;
import com.ylivirta.util.Util;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.remain.Remain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerDatabase extends ModelDatabase {

	private final Set<UUID> successfulJoin = ConcurrentHashMap.newKeySet();
	private final Set<UUID> playersInSync = ConcurrentHashMap.newKeySet();
	private final static PlayerDatabase INSTANCE = new PlayerDatabase();

	public static PlayerDatabase getInstance() {
		return INSTANCE;
	}

	public void setup() {
		final Connection connection = getConnection();

		Valid.checkNotNull(connection, Settings.Translation
				.getLangKeyReplace("is_null","MySQL connection")
		);

		PreparedStatement statement = null;

		try {
			String data = "CREATE TABLE IF NOT EXISTS "
					+ Settings.MySQL.MYSQL_TABLE + " "
					+ "(id int(10) AUTO_INCREMENT, player_uuid char(36) NOT NULL UNIQUE, "
					+ "player_name varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, "
					+ "health double(3,1) NOT NULL, health_scale double(3,1) NOT NULL, "
					+ "food_level int(10) NOT NULL, saturation float(10,8) NOT NULL, "
					+ "total_exp int(20) NOT NULL, "
					+ "end_chest LONGTEXT NOT NULL, inventory LONGTEXT NOT NULL, armor LONGTEXT NOT NULL, "
					+ "last_seen char(13) NOT NULL, PRIMARY KEY(id));";
			statement = connection.prepareStatement(data);
			statement.execute();
		} catch (SQLException exception) {
			Common.log(Settings.Translation.getLang("setup"));
			Common.log(exception.getMessage());
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception exception) {
				Common.log(exception.getMessage());
			}
		}
	}

	public boolean hasAccount(UUID uid) {
		ResultSet result = null;
		PreparedStatement statement = null;
		final Connection connection = getConnection();

		Valid.checkNotNull(connection,
			Settings.Translation.getLangKeyReplace("is_null","MySQL connection")
		);

		try {
			statement = connection.prepareStatement("SELECT `player_uuid` FROM "
					+ Settings.MySQL.MYSQL_TABLE + " "
					+ " WHERE `player_uuid` = ?"
			);
			statement.setString(1, uid.toString());
			result = statement.executeQuery();

			if (result.next())
				return true;

		} catch (SQLException exception) {
			Common.log(Settings.Translation.getLang("has_account"));
			Common.log(exception.getMessage());
		} finally {
			try {
				if (result != null)
					result.close();
				if (statement != null)
					statement.close();
			} catch (Exception exception) {
				Common.log(exception.getMessage());
			}
		}
		return false;
	}

	public void createAccount(final UUID uid, final Player player) {
		PreparedStatement statement = null;
		final Connection connection = getConnection();

		Valid.checkNotNull(connection, Settings.Translation
				.getLangKeyReplace("is_null","MySQL connection")
		);

		try {
			statement = connection.prepareStatement(
				"INSERT INTO "
						+ Settings.MySQL.MYSQL_TABLE
						+ " (`player_uuid`, `player_name`, `health`, `health_scale`, `food_level`, `saturation`, `total_exp`, `end_chest`, `inventory`, `armor`, `last_seen`) "
						+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			statement.setString(1, uid.toString());

			if (player == null) {
				statement.setString(2, "none");
			} else {
				statement.setString(2, player.getName() + "");
			}

			statement.setDouble(3, 0);
			statement.setDouble(4, 0);
			statement.setInt(5, 0);
			statement.setFloat(6, 0);
			statement.setInt(7, 0);
			statement.setString(8, "none");
			statement.setString(9, "none");
			statement.setString(10, "none");
			statement.setString(11, String.valueOf(System.currentTimeMillis()));
			statement.executeUpdate();

		} catch (SQLException exception) {
			Common.log(Settings.Translation.getLang("create_account"));
			Common.log(exception.getMessage());
		} finally {
			try {
				if (statement != null)
					statement.close();
			} catch (Exception exception) {
				Common.log(exception.getMessage());
			}
		}
	}

	public void setData(final UUID uid, final Player player,
						double health, double healthScale,
						int foodLevel, float saturation, int totalExp,
						String endChest, String inventory, String armor) {

		PreparedStatement statement = null;
		final Connection connection = getConnection();

		Valid.checkNotNull(connection, Settings.Translation
				.getLangKeyReplace("is_null","MySQL connection")
		);

		try {
			statement = connection.prepareStatement(
					"UPDATE "
					+ Settings.MySQL.MYSQL_TABLE
					+ " SET `player_name` = ?"
					+ ", `health` = ?"
					+ ", `health_scale` = ?"
					+ ", `food_level` = ?"
					+ ", `saturation` = ?"
					+ ", `total_exp` = ?"
					+ ", `end_chest` = ?"
					+ ", `inventory` = ?"
					+ ", `armor` = ?"
					+ ", `last_seen` = ?"
					+ " WHERE `player_uuid` = ?");

			if (player == null) {
				statement.setString(1, "none");
			} else {
				statement.setString(1, player.getName() + "");
			}

			statement.setDouble(2, health);
			statement.setDouble(3, healthScale);
			statement.setInt(4, foodLevel);
			statement.setFloat(5, saturation);
			statement.setInt(6, totalExp);
			statement.setString(7, endChest);
			statement.setString(8, inventory);
			statement.setString(9, armor);
			statement.setString(10, String.valueOf(System.currentTimeMillis()));
			statement.setString(11, uid.toString());
			statement.executeUpdate();

		} catch (SQLException exception) {
			Common.log(Settings.Translation.getLang("set_data"));
			Common.log(exception.getMessage());
		} finally {
			try {
				if (statement != null)
					statement.close();
			} catch (Exception exception) {
				Common.log(exception.getMessage());
			}
		}
	}

	public ConcurrentMap<String, Any<?>> getData(UUID uid) {
		ResultSet result = null;
		PreparedStatement statement = null;
		final Connection connection = getConnection();

		Valid.checkNotNull(connection, Settings.Translation
				.getLangKeyReplace("is_null","MySQL connection")
			);

		ConcurrentMap<String, Any<?>>  values = new ConcurrentHashMap<>();

		try {
			statement = connection.prepareStatement("SELECT * FROM "
					+ Settings.MySQL.MYSQL_TABLE
					+ " WHERE `player_uuid` = ? LIMIT 1");
			statement.setString(1, uid.toString());
			result = statement.executeQuery();

			while (result.next()) {
				values.put("health", new Any<>(result.getDouble("health")));
				values.put("health_scale", new Any<>(result.getDouble("health_scale")));
				values.put("food_level", new Any<>(result.getInt("food_level")));
				values.put("saturation", new Any<>(result.getFloat("saturation")));
				values.put("total_exp", new Any<>(result.getInt("total_exp")));
				values.put("end_chest", new Any<>(result.getString("end_chest")));
				values.put("inventory", new Any<>(result.getString("inventory")));
				values.put("armor", new Any<>(result.getString("armor")));
			}

			return values;
		}  catch (SQLException exception) {
			Common.log(Settings.Translation.getLang("get_data"));
			Common.log(exception.getMessage());
		}  finally {
			try {
				if (result != null)
					result.close();
				if (statement != null)
					statement.close();
			} catch (Exception exception) {
				Common.log(exception.getMessage());
			}
		}
		return null;
	}

	public void onStop() {
		String playerData = Settings.Translation.getLang("player_data");
		Common.log(
				Settings.Translation.getLangKeyReplace("save_all_start", playerData)
		);
		List<Player> onlinePlayers = new ArrayList<>(Remain.getOnlinePlayers());

		for (Player player : onlinePlayers)
		{
			if (player.isOnline()) {
				final UUID uid = player.getUniqueId();
				final double healthScale = player.getHealthScale();
				final double health = player.getHealth();
				final float saturation = player.getSaturation();
				final int foodLevel = player.getFoodLevel();
				final int totalExp = SetExpFix.getTotalExperience(player);
				final Inventory endChest = player.getEnderChest();
				final Inventory inv = player.getInventory();
				EntityEquipment armor = player.getEquipment();
				ItemStack[] contents = armor.getArmorContents();

				deleteSync(uid);
				setData(uid, player,
						health, healthScale,
						foodLevel, saturation,
						totalExp,
						Util.encodeItems(endChest.getContents()),
						Util.encodeItems(inv.getContents()),
						Util.encodeItems(armor.getArmorContents())
				);
			}
		}

		Common.log(Settings.Translation.getLangKeyReplace("save_all_end", playerData));
	}

	public void closeConnection() {
		Connection connection = getConnection();

		Valid.checkNotNull(connection, Settings.Translation
				.getLangKeyReplace("is_null","MySQL connection")
		);

		try {
			connection.close();
		} catch (SQLException exception) {
			Common.log(exception.getMessage());
		}
	}

	public boolean isSyncComplete(UUID uid) {
		return playersInSync.contains(uid);
	}

	public void completeSync(UUID uid) {
		playersInSync.add(uid);
	}

	public void deleteSync(UUID uid) {
		playersInSync.remove(uid);
	}

	public boolean successfulJoinHas(UUID uid) {
		return successfulJoin.contains(uid);
	}

	public void successfulJoinAdd(UUID uid) {
		successfulJoin.add(uid);
	}

	public void successfulJoinDelete(UUID uid) {
		successfulJoin.remove(uid);
	}
}
