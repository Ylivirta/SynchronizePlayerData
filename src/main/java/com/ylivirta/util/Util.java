package com.ylivirta.util;

import com.ylivirta.settings.Settings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.mineacademy.fo.Common;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Util
{
	public static String encodeItems(ItemStack[] items) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			dataOutput.writeInt(items.length);

			for (ItemStack item : items) {
				dataOutput.writeObject(item);
			}

			dataOutput.close();

			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception e) {
			Common.throwError(
					new IllegalStateException(),
					Settings.Translation.getLang("cant_save_item_stack")
			);
		}
		return null;
	}

	public static ItemStack[] decodeItems(String data) {
		try {
			try {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
				BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
				ItemStack[] items = new ItemStack[dataInput.readInt()];

				for (int i = 0; i < items.length; i++) {
					items[i] = (ItemStack) dataInput.readObject();
				}

				dataInput.close();
				return items;
			} catch (ClassNotFoundException e) {
				Common.throwError(
					new IllegalStateException(),
					Settings.Translation.getLang("cant_decode_class_type")
				);
			}
		} catch (Exception exception) {
			Common.log(exception.getMessage());
		}
		return null;
	}
}
