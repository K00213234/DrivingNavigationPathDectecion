package drivingNavigationPathDectecion;

import java.util.ArrayList;

public class LogService {

	public LogService() {
		//this.TokenDefault = new LogToken(this);
		this.TokenDefault = null;
		
		// this.Token1 = new LogToken(this);
		// this.Token2 = new LogToken(this);
		// this.Token3 = new LogToken(this);
		this.DebuggingAtAngleToken = new LogToken(this);
	}

	public final LogToken TokenDefault;
	
//	public final LogToken Token1;
//	public final LogToken Token2;
//	public final LogToken Token3;
	public final LogToken DebuggingAtAngleToken;


	public void log(String message) {
		log(message, TokenDefault);
	}

	public void log(String eventName, String message) {
		log(eventName + message);
	}

	public void log(String message, LogToken token) {
		if (tokenIsAllowed(token)) {
			System.out.println(message);
		}
	}

	public void addToken(LogToken token) {
		tokenList.add(token);
	}

	private Boolean tokenIsAllowed(LogToken token) {
		return (token != null) && (this.tokenList.indexOf(token) != -1);
	}

	private final ArrayList<LogToken> tokenList = new ArrayList<LogToken>();
}