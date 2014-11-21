package it.xpug.mastermind.main;

import java.text.DecimalFormat;
import java.util.Map;

import it.xpug.generic.db.Database;
import it.xpug.generic.db.ListOfRows;

public class RankingRepository {

	private Database database;

	public RankingRepository(Database database){
		this.database = database;
	}

	//Lettura punteggio utente
	public String getRating(String username) {
		String gameCount = "0";
		String score="";
		String sql = "select avg(tries) as average, count(*) as ngames from games where username=? and ( status='win' or status='lose' )";
		ListOfRows rows = database.select(sql, username);
		if(rows.size()!=0) {
			if(rows.get(0).get("average")!=null){
				DecimalFormat decimal = new DecimalFormat("#.##"); 
				score = decimal.format(Double.parseDouble(""+rows.get(0).get("average")));
				gameCount = String.valueOf(rows.get(0).get("ngames"));
			}
		}
		return score + " on " + gameCount + " games";
	}

	//Lettura classifica generale ordinata per punteggio
	public ListOfRows selectRanking() {
		String sql = "select username, avg(tries) as average, count(*) as ngames from games where status='win' or status='lose' group by username order by average";
		ListOfRows rows = database.select(sql);
		return rows;
	}
}
