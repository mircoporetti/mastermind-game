package it.xpug.mastermind.main;


import static java.lang.Integer.*;
import it.xpug.generic.db.*;
import it.xpug.generic.web.*;


public class Main {
	public static void main(String[] args) {
		String port = System.getenv("PORT");
		if (port == null || port.isEmpty()) {
			port = "8080";
		}

		DatabaseConfiguration configuration;
		if (System.getenv("DATABASE_URL") != null)
			configuration = new HerokuDatabaseConfiguration();
		else
			configuration = new PropertyFileDatabaseConfiguration("database.properties");
		
		ReusableJettyApp app = new ReusableJettyApp(new MastermindServlet(configuration));
		app.start(valueOf(port), "src/main/webapp");
	}
}
