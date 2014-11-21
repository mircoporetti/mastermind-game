package it.xpug.mastermind.main;

import it.xpug.generic.db.Database;
import it.xpug.generic.db.ListOfRows;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class SessionRepository {
	private Database database;
	
	public SessionRepository(Database database){
		this.database = database;
	}
	
	//Salvataggio sessione
	public void storeSession(PlayerSession session){
		String sql = "insert into sessions (idsession, username) values (?, ?)";
		database.execute(sql, session.sessionId(), session.username());
		
	}

	//Restituisce l'username dell'utente relativo alla sessione indicata
	public String findSession(String sessionId){
		String sql = "select username from sessions where idsession = ?";
		ListOfRows rows = database.select(sql, Integer.parseInt(sessionId));
		if(rows.size() != 0){
			Map<String, Object> userMap = rows.get(0);
			String username = (String) userMap.get("username");
			return username;
		}
		else return null;
	}
}
