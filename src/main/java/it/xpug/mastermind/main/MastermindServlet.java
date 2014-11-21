package it.xpug.mastermind.main;


import it.xpug.generic.db.*;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class MastermindServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DatabaseConfiguration configuration;

	public MastermindServlet(DatabaseConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Database database = new Database(configuration);
		SessionRepository sessionRepository = new SessionRepository(database);
		AuthenticationRepository authenticationRepository = new AuthenticationRepository(database);
		GameRepository gameRepository = new GameRepository(database);
		RankingRepository rankRepository = new RankingRepository(database);
		MastermindController controller = new MastermindController(sessionRepository,gameRepository, request, response, authenticationRepository, rankRepository);
		controller.service();
	}
}
