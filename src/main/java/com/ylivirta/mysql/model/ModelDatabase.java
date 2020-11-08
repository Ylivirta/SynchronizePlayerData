package com.ylivirta.mysql.model;

import org.mineacademy.fo.Common;
import org.mineacademy.fo.database.SimpleDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ModelDatabase extends SimpleDatabase {

	public void keepAlive() {
		Connection connection = getConnection();

		if (connection != null)
		{
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1");
				preparedStatement.executeQuery();
				preparedStatement.close();
			} catch (SQLException exception) {
				Common.log(exception.getMessage());
			}
		}
	}
}
