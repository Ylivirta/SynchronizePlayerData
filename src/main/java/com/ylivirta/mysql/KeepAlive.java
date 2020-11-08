package com.ylivirta.mysql;

import com.ylivirta.mysql.impl.PlayerDatabase;

public class KeepAlive implements Runnable {

	private final static PlayerDatabase PLAYER_DATABASE = PlayerDatabase.getInstance();

	@Override
	public void run() {
		PLAYER_DATABASE.keepAlive();
	}
}