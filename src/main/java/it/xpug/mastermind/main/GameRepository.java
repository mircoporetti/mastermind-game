package it.xpug.mastermind.main;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import it.xpug.generic.db.Database;
import it.xpug.generic.db.ListOfRows;

public class GameRepository {
	Database database;

	public GameRepository(Database database){
		this.database = database;
	}

	//Salvataggio nuova partita
	public int storeNewGame(Game game){
		String sql = "insert into games (username,sequence, status) values (?, ?, ?)";
		database.execute(sql, game.username(),game.sequence(), game.status());

		int idGame = 0;
		String sql2 = "select MAX(idgame) as max from games where username = ?";
		ListOfRows rows = database.select(sql2, game.username());
		if(rows.size()!=0) {
			Map<String, Object> userMap = rows.get(0);
			idGame = (Integer) userMap.get("max");
		}
		return idGame;

	}


	//Selezione stato partita
	public String checkGameStatus(String idGame) {
		String sql = "select status from games where idgame=?";
		ListOfRows rows = database.select(sql, Integer.parseInt(idGame));
		Map<String, Object> map = rows.get(0);
		String status = "" + map.get("status");
		return status;
	}

	//Salvataggio tentativo
	public void saveTries(int idGame, int trySequence, String result) {
		String sql = "insert into tries (try, idGame, result) values (?, ?, ?)";
		database.execute(sql, trySequence, idGame, result);
	}

	//settaggio stato a fine partita
	public void setFinished(int idGame, String status, int tries) {
		String dateFinished;
		String sql;
		if (status.equals("notfinished")){
			sql = "update games set status='" + status + "' where idgame=" + idGame + " and status ='playing'";
		}
		else{
			sql = "update games set status='" + status +"', finished='" + new Timestamp(new Date().getTime()) + "', tries='" + tries +"' where idgame=" + idGame;
		}
		database.execute(sql);
	}

	public int getIdTry(int idGame) {
		String sql = "select * from games natural join tries where idgame=?";
		ListOfRows rows = database.select(sql, idGame);
		if(rows.size()!=0) {
			int count = rows.size();
			return count;
		}
		else return 0;
	}

	public String getGameSequence(int idGame) {
		String sql = "select sequence from games where idgame=?";
		ListOfRows rows = database.select(sql, idGame);
		Map<String, Object> map = rows.get(0);
		String sequence = (String) map.get("sequence");
		return sequence;
	}
	
	//Selezione partite di un giocatore
	public ListOfRows getPlayerGames(String username){
		String sql = "select * from games where username = ? AND status != 'playing' order by started";
		ListOfRows rows = database.select(sql, username);
		return rows;
	}


}
