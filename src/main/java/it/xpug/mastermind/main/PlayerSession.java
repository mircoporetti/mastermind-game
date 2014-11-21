package it.xpug.mastermind.main;

import java.security.MessageDigest;

public class PlayerSession {
	private int sessionId;
	private String username;
	private String password;
	
	public PlayerSession(String username, String password){
		this.username = username;
		this.sessionId = (int) (Math.random()* 10000);
		this.password = password;
	}

	public int sessionId(){
		return this.sessionId;
	}
	
	public String username(){
		return this.username;
	}
	
	//Criptaggio password
	public String encryptedPassword() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return toHexString(digest);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Conversione in Hex
	static String toHexString(byte [] bytes) {
		String symbols = "0123456789abcdef";
		String result = "";
		for (byte b : bytes) {
			int i = b & 0xFF;
			result += symbols.charAt(i / 16);
			result += symbols.charAt(i % 16);
		}
		return result;
	}
}
