/*
 * This Java class is from: https://hub.spigotmc.org/jenkins/job/Spigot-Essentials/
 *
 * It is not an original creation of github user: Ylivirta
 */
package com.ylivirta.util;

import com.ylivirta.settings.Settings;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;

public class SetExpFix {

	public static void setTotalExperience(final Player player, final int exp) {
		if (exp < 0) {
			Common.throwError(new ArithmeticException(), Settings.Translation.getLang("negative_exp"));
			return;
		}
		player.setExp(0);
		player.setLevel(0);
		player.setTotalExperience(0);

		int amount = exp;
		while (amount > 0) {
			final int expToLevel = getExpAtLevel(player);
			amount -= expToLevel;
			if (amount >= 0) {
				// give until next level
				player.giveExp(expToLevel);
			} else {
				// give the rest
				amount += expToLevel;
				player.giveExp(amount);
				amount = 0;
			}
		}
	}

	private static int getExpAtLevel(final Player player) {
		return getExpAtLevel(player.getLevel());
	}

	public static int getExpAtLevel(final int level) {
		if (level <= 15) {
			return (2 * level) + 7;
		}
		if ((level <= 30)) {
			return (5 * level) - 38;
		}
		return (9 * level) - 158;
	}

	public static int getTotalExperience(final Player player) {
		int exp = Math.round(getExpAtLevel(player) * player.getExp());
		int currentLevel = player.getLevel();

		while (currentLevel > 0) {
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		}
		if (exp < 0) {
			exp = 0;
		}
		return exp;
	}
}
