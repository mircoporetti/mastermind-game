package it.xpug.mastermind.main;

import static java.lang.String.*;
import it.xpug.generic.db.ListOfRows;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.servlet.http.*;



public class MastermindController {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private SessionRepository sessionRepository;
	private AuthenticationRepository authenticationRepository;
	private GameRepository gameRepository;
	private RankingRepository rankRepository;
	SimpleDateFormat noMilliSecondsFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//Inizializzazione controller con i repository
	public MastermindController(SessionRepository sessionRepository, GameRepository gameRepository,
			HttpServletRequest request, HttpServletResponse response, AuthenticationRepository authenticationRepository,
			RankingRepository rankRepository) {
		this.sessionRepository = sessionRepository;
		this.request = request;
		this.response = response;
		this.authenticationRepository = authenticationRepository;
		this.gameRepository = gameRepository;
		this.rankRepository = rankRepository;
	}

	//Gestione chiamate 
	public void service() throws IOException {
		response.setContentType("application/json");
		if(request.getRequestURI().equals("/signup"))
			doRegistration();
		else if (request.getRequestURI().equals("/authenticate"))
			doAuthenticate();
		else if (request.getRequestURI().equals("/checkSession"))
			doCheckSession();
		else if (request.getRequestURI().equals("/newGame"))
			doNewGame();
		else if (request.getRequestURI().equals("/notFinished"))
			doNotFinished();
		else if (request.getRequestURI().equals("/try"))
			doTry();
		else if (request.getRequestURI().equals("/ranking"))
			doRank();
		else if (request.getRequestURI().equals("/history"))
			doHistory();
		else if (request.getRequestURI().equals("/checkStatus"))
			doCheckStatus();
		else if (request.getRequestURI().equals("/rateNav"))
			rateNav();
		else 
			do404();
	}

	//controllo stato partita
	private void doCheckStatus() throws IOException {
		String idGame = request.getParameter("idGame");
		String gameStatus = gameRepository.checkGameStatus(idGame);
		
		response.setStatus(200);
		writeBody("{ \"gameStatus\": \""+ gameStatus + "\" }");
	}
	
	//Classifica generale
	private void doRank() throws IOException {
		ListOfRows rows = rankRepository.selectRanking();
		String[] name={"pos", "id","avg", "games"};
		String json="[";
		
		for (int i = 0; i < rows.size(); i++){
			DecimalFormat decimal = new DecimalFormat("#.##"); 
			String score = decimal.format(Double.parseDouble(""+rows.get(i).get("average")));
			String[] value = {""+(i+1), ""+rows.get(i).get("username"),score, ""+rows.get(i).get("ngames")};
			if (i == 0) {
				json = json + toJson(name,value);
			} else {
				json += "," + toJson(name,value);
			}
		}
		
		json = json + "]";
		String toJsoned = toJson("players", json);
		toJsoned = toJsoned.replace("\"[", "[");
		toJsoned = toJsoned.replace("]\"", "]");
		System.out.println(toJsoned);
		writeBody(toJsoned);
	}
	
	//Storia partite di un singolo utente
	private void doHistory() throws IOException{
		String username = request.getParameter("username");
		ListOfRows rows = gameRepository.getPlayerGames(username);
		String[] name={"started", "finished","tries", "status"};
		String json="[";
		
		for (int i = 0; i < rows.size(); i++){
			String started= noMilliSecondsFormatter.format((Timestamp) rows.get(i).get("started"));
			String finished;
			String tries;
			String status;
			if(rows.get(i).get("tries")!= null){
			tries = String.valueOf(rows.get(i).get("tries"));
			status = String.valueOf(rows.get(i).get("status"));
			finished = noMilliSecondsFormatter.format((Timestamp) rows.get(i).get("finished"));;
			}
			else{
				finished = "Not Finished";
				tries = "None";
				status = "Not finished";
			}
			
			String[] value = {started+"", finished, ""+tries, status};
			if (i == 0) {
				json = json + toJson(name,value);
			} else {
				json += "," + toJson(name,value);
			}
		}
		
		json = json + "]";
		String toJsoned = toJson("history", json);
		toJsoned = toJsoned.replace("\"[", "[");
		toJsoned = toJsoned.replace("]\"", "]");
		System.out.println(toJsoned);
		writeBody(toJsoned);
	}

	//Settaggio stato partita a notfinished
	private void doNotFinished(){
		String idGame = request.getParameter("idGame");
		gameRepository.setFinished(Integer.parseInt(idGame), "notfinished", 0);
	}

	//Registrazione nuovo utente se non già esistente
	private void doRegistration() throws IOException {
		try{
			String username = request.getParameter("id");
			String password = request.getParameter("password");
			String repeatPassword = request.getParameter("repeatPassword");
			String email = request.getParameter("email");
			if(username.trim().isEmpty() || password.length() < 8 || password.trim().isEmpty() || email.trim().isEmpty() || authenticationRepository.checkUsername(username)
					|| !email.contains("@") || !password.equals(repeatPassword)){
				response.setStatus(400);
				writeBody("{ \"description\": \" Registration Unsuccessfull \" }");

			} else {
				PlayerSession player = new PlayerSession(username, password);
				String encryptedPassword = player.encryptedPassword();
				authenticationRepository.registerPlayer(username, encryptedPassword, email);
				response.setStatus(200);
				writeBody("{ \"description\": \"Registration Successfull\" }");
			}
		} catch(Exception e) {
			response.setStatus(400);
			writeBody("{ \"description\": \"Registration Unsuccessfull\" }");
		}
	}

	//Autenticazione e creazione Cookie per la sessione
	private void doAuthenticate() throws IOException {
		String username = request.getParameter("id");
		String password = request.getParameter("password");
		PlayerSession session = new PlayerSession(username, password);
		boolean logged = authenticationRepository.login(username, session.encryptedPassword());
		
		if(logged){
			sessionRepository.storeSession(session);
			Cookie cookie = new Cookie("session_id", String.valueOf(session.sessionId()));
			String rate = rankRepository.getRating(username);
			
			String[] name = new String[3];
			String[] value = new String [3];
			name[0]= "session_id";
			name[1]= "username";
			name[2]= "rate";
			value[0]= String.valueOf(session.sessionId());
			value[1]= username;
			value[2]= rate;
			
			response.addCookie(cookie);
			response.setStatus(200);
			String json = toJson(name,value);
			writeBody(json);
		}
		else {
			response.setStatus(401);
			writeBody("{ \"description\": \"Bad Id or Password\" }");
		}
	}

	//Controllo di una sessione esistente al caricamento della pagina
	private void doCheckSession() throws IOException {
		Cookie[] cookies = request.getCookies();
		if (cookies!=null){
			String sessionId = "";
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("session_id")){
					sessionId = cookie.getValue();
					break;
				}
			}
			if (!sessionId.equals(""))  {
				String username = sessionRepository.findSession(sessionId);
				if(username!=null){
					
					String[] name = new String[3];
					String[] value = new String [3];
					name[0]= "session_id";
					name[1]= "username";
					value[0]= String.valueOf(sessionId);
					value[1]= username;
					
					response.setStatus(200);
					String json = toJson(name,value);
					writeBody(json);
				}
				else {
					response.setStatus(400);
					writeBody("{ \"description\": \"Cookie not found\" }");
				}
			}
			else {
				response.setStatus(400);
				writeBody("{ \"description\": \"Cookie not found\" }");
			}
		}
		else {
			response.setStatus(400);
			writeBody("{ \"description\": \"Cookie not found\" }");
		}
	}

	//Creazione nuova partita
	private void doNewGame() throws IOException {
		String sessionId = request.getParameter("sessionId");
		String username = sessionRepository.findSession(sessionId);
		String sequence = createSequence();
		Game game = new Game(username, sequence, "playing");
		int idGame = gameRepository.storeNewGame(game);
		if (idGame!= 0){
			response.setStatus(200);
			writeBody(format("{ \"id_game\": %s }", idGame));
		}
		else{
			response.setStatus(400);
			writeBody("{ \"description\": \"idGame problem\" }");
		}
	}

	//Gestione tentativi all'interno di una partita
	private void doTry() throws IOException {
		try{
			String sequence = request.getParameter("sequence");
			int idGame = Integer.parseInt(request.getParameter("idGame"));
			int id = gameRepository.getIdTry(idGame);
			if(id<10) {
				Cookie[] cookies = request.getCookies();
				String sessionId = "";
				for (Cookie cookie : cookies) { //fare un metodo dato che lo usiamo anche in checksession
					if (cookie.getName().equals("session_id")){
						sessionId = cookie.getValue();
						break;
					}
				}

				boolean control = sequenceControl(sequence);
				if(sequence.length()==4 && control){
					String gameSequence = gameRepository.getGameSequence(idGame);
					String result = calculateResult(sequence, gameSequence);
					gameRepository.saveTries(idGame, Integer.parseInt(sequence), result);
					id = gameRepository.getIdTry(idGame);
					if(result.equals("++++")) gameRepository.setFinished(idGame, "win", id);

					String[] name = new String[3];
					String[] value = new String [3];
					name[0]= "sequenceNumber";
					name[1]= "sequence";
					name[2]= "result";
					value[0]=String.valueOf(id);
					value[1]=sequence;
					value[2]=result;

					response.setStatus(200);
					String json = toJson(name,value);
					writeBody(json);
				}
				else{
					response.setStatus(400);
					writeBody("{ \"description\": \"Sequence must be composed by 4 numbers {1,2,3,4,5,6} \" }");
				}
			}
			else{
				gameRepository.setFinished(idGame, "lose", 10);
				response.setStatus(400);
				writeBody("{ \"description\": \"You have no more tries\" }");
			}
		}
		catch(NumberFormatException nfe){
			response.setStatus(400);
			writeBody("{ \"description\": \"Invalid character, only numbers are allowed\" }");
		}
	}

	
	//Controllo su eventuali cifre non permesse nel tentativo
	private boolean sequenceControl(String seq) {
		boolean control = true;
		if(seq.contains("0")) {
			control = false;
		} 
		else {
			for(int i=7; i<=9; i++) {
				if(seq.contains(String.valueOf(i))){
					control =  false;
					break;
				}
			}
		}
		return control;
	}

	
	//Calcolo del risultato di un tentativo
	private String calculateResult(String userSeq, String gameSeq) {
		System.out.println("gameseq: " + gameSeq);
		System.out.println("seq: " + userSeq);
		String sequence = userSeq;
		String gameSequence = gameSeq;
		String rightPosition = "";
		String wrongPosition = "";

		for (int i = 0; i < sequence.length(); i++) {
			if(sequence.charAt(i)==gameSequence.charAt(i)){
				rightPosition = rightPosition + "+";
				char [] tempGameSequence = gameSequence.toCharArray();
				tempGameSequence[i] = 'y';
				gameSequence = String.valueOf(tempGameSequence);
				char [] tempSequence = sequence.toCharArray();
				tempSequence[i] = 'z';
				sequence = String.valueOf(tempSequence);
			}
		}

		for (int i = 0; i < sequence.length(); i++) {
			for (int j = 0; j < gameSequence.length(); j++) {
				if(i!=j && sequence.charAt(i)==gameSequence.charAt(j)){
					wrongPosition = wrongPosition + "-";
					char [] tempGameSequence = gameSequence.toCharArray();
					tempGameSequence[j] = 'y';
					gameSequence = String.valueOf(tempGameSequence);
					break;
				}
			}
		}

		String result = rightPosition + wrongPosition;
		return result;
	}
	
	//Creazione sequenza da indovinare
	private String createSequence(){
		String sequence = "";
		for (int i = 0; i < 4; i++) {
			int number = (int) Math.floor((Math.random() * 6)+1);
			sequence = sequence + number;
		}
		return sequence;
	}

	//aggiornamento nav bar con nuovo punteggio
	private void rateNav() throws IOException {
		String sessionId = request.getParameter("sessionId");
		String username = sessionRepository.findSession(sessionId);
		if(username!=null){
			String rate = rankRepository.getRating(username);
			
			String[] name = new String[3];
			String[] value = new String [3];
			name[0]= "username";
			name[1]= "rate";
			value[0]=username;
			value[1]=rate;

			response.setStatus(200);
			String json = toJson(name,value);
			writeBody(json);
		}
	}

	
	private void do404() throws IOException {
		response.setStatus(404);
		writeBody(toJson("description", "Not Found"));
	}

	
	//Metodi utili per JSon
	private String toJson(String name, String value) {
		return format("{ \"%s\": \"%s\" }", name, value);
	}

	protected String toJson(String[] name, String[] value) {
		String json="";
		for (int i=0; i<name.length; ++i){
			if (i == 0) {
				json+= format("\"%s\": \"%s\" ", name[i], value[i]);
			} else {
				json+= "," + format("\"%s\": \"%s\" ", name[i], value[i]);
			}

		}
		return "{" +json+ "}";
	}

	private void writeBody(String body) throws IOException {
		response.getWriter().write(body);
	}

}