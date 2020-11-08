package com.ylivirta.settings;

import org.mineacademy.fo.Common;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.settings.SimpleSettings;

import java.util.*;

public class Settings extends SimpleSettings {

	@Override
	protected int getConfigVersion() {
		return 1;
	}

	public static class MySQL {
		public static final String MYSQL_TABLE = "SynchronizePlayerData";
		public static String MYSQL_HOST;
		public static String MYSQL_USERNAME;
		public static String MYSQL_DATABASE;
		public static String MYSQL_PASSWORD;
		public static Integer MYSQL_PORT;
		public static Integer MYSQL_DELAY;

		private static void init() {
			pathPrefix("MySQL");

			MYSQL_HOST = getString("host");
			MYSQL_PORT = getInteger("port");
			MYSQL_DATABASE = getString("database");
			MYSQL_USERNAME = getString("username");
			MYSQL_PASSWORD = getString("password");
			MYSQL_DELAY = getInteger("delay");
		}
	}

	public static class Translation {
		public static String RELOAD_SUCCESS;
		public static String RELOAD_FAIL;
		public static SerializedMap LANG = new SerializedMap();

		private static void init() {
			pathPrefix("Translation");

			RELOAD_SUCCESS = getString("reload_success");
			RELOAD_FAIL = getString("reload_fail");

			final List<String> values = getStringList("lang");

			if (values.size() != 9) {
				Common.throwError(new Exception(), "Translation is missing fields!");
			}

			String translatedValue = "";

			for (String line : values) {
				String[] splitValues = line.split("=");
				if (splitValues.length != 2) {
					Common.throwError(new Exception(), "Line: " + line + " is missing delimiter ':'");
				}

				String key = splitValues[0].replace("{", "");
				String value = splitValues[1].replace("}", "");

				if ("function_failed".equals(key)) {
					translatedValue = value;
				} else {
					LANG.put(key, value);
				}
			}

			if (translatedValue.length() > 0)
			{
				LANG.put("setup", translatedValue.replace("&key&", "void setup()"));
				LANG.put("has_account", translatedValue.replace("&key&", "boolean hasAccount(UUID uid)"));
				LANG.put("create_account", translatedValue.replace("&key&",
						"boolean createAccount(final UUID uid, final Player player)")
				);
				LANG.put("get_data", translatedValue.replace("&key&",
						"HashMap<String, String> getData(UUID uid)")
				);
				LANG.put("set_data", translatedValue.replace("&key&",
						"void setData(final UUID uid, final Player player,\n" +
								"\t\t\t\t\t\tdouble health, double healthScale,\n" +
								"\t\t\t\t\t\tint foodLevel, float saturation, float exp,\n" +
								"\t\t\t\t\t\tint expToLevel, int totalExp, int expLevel,\n" +
								"\t\t\t\t\t\tString endChest, String inventory, String armor)")
				);
			}

			Debugger.detectDebugMode();

			if (Debugger.isDebugModeEnabled()) {
				for (Map.Entry<String, Object> entry : LANG.entrySet()) {
					Common.log(entry.getKey() + ":" + "" + entry.getValue());
				}
			}
		}

		public static String getLang(String text) {
			return LANG.getString(text);
		}

		public static String getLangKeyReplace(String text, String replacement)
		{
			String translatedValue = LANG.getString(text);
			return translatedValue.replace("&key&", replacement);
		}

		public static void clear() {
			Iterator<String> it = LANG.keySet().iterator();

			while (it.hasNext()) {
				it.next();
				it.remove();
			}
		}
	}

	private static void init() {
		pathPrefix(null);
	}
}
