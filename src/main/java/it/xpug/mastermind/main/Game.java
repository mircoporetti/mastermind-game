package it.xpug.mastermind.main;

public class Game {
	private String username;
	private String sequence;
	private String status;

	public Game(String username,String sequence, String status){
		this.username = username;
		this.sequence = sequence;
		this.status = status;
	}
	
	public String username(){
		return this.username;
	}
	
	public String status(){
		return this.status;
	}
	
	public String sequence(){
		return this.sequence;
	}
}
