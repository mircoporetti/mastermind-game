package it.xpug.mastermind.main;

import it.xpug.generic.db.Database;
import it.xpug.generic.db.ListOfRows;

public class AuthenticationRepository {
	
private Database database;
	
	public AuthenticationRepository(Database database){
		this.database = database;
	}

	//Salvataggio utente
	public void registerPlayer(String username, String password, String email) {
		String sql = "insert into users (username, email, password) values (?, ?, ?)";
		database.execute(sql, username, email, password);
	}
	
	//Selezione username e password usati per login
	public boolean login (String username, String password) {
		String sql = "select * from users where username = ? and password = ?";
		ListOfRows rows = database.select(sql, username, password);
		return rows.size() != 0;
	}
	
	//Controllo username
	public boolean checkUsername (String username) {
		String sql = "select * from users where username = ?";
		ListOfRows rows = database.select(sql, username);
		return rows.size() != 0;
	}
}
