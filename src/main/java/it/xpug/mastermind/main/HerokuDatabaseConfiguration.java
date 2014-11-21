package it.xpug.mastermind.main;

import java.net.*;
import java.sql.*;

public class HerokuDatabaseConfiguration implements DatabaseConfiguration {

	@Override
	public Connection getConnection() {
		try {
			URI dbUri = new URI(System.getenv("DATABASE_URL"));
		    String username = dbUri.getUserInfo().split(":")[0];
		    String password = dbUri.getUserInfo().split(":")[1];
		    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
		    return DriverManager.getConnection(dbUrl, username, password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
