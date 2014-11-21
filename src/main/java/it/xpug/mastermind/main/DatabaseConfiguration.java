package it.xpug.mastermind.main;

import java.sql.*;

public interface DatabaseConfiguration {
	Connection getConnection();
}