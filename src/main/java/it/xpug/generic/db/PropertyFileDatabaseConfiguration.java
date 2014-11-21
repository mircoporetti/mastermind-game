package it.xpug.generic.db;

import static java.lang.String.*;
import it.xpug.mastermind.main.*;

import java.io.*;
import java.sql.*;
import java.util.*;

public class PropertyFileDatabaseConfiguration implements DatabaseConfiguration {

	private Properties properties;

	public PropertyFileDatabaseConfiguration(String fileName) {
		this.properties = getProperties(fileName);
	}

	public Connection getConnection() {
		try {
			return tryGetConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Connection tryGetConnection() throws ClassNotFoundException, SQLException {
		String host = properties.getProperty("host");
		String database = properties.getProperty("database");
		String url = format("jdbc:postgresql://%s/%s", host, database);
		Class.forName("org.postgresql.Driver");
		Connection connection = DriverManager.getConnection(url, properties);
		connection.setAutoCommit(false);
		return connection;
	}

	private Properties getProperties(String fileName) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(fileName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return properties;
	}

}
