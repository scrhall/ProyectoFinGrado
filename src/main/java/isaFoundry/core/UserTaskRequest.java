package isaFoundry.core;


import java.util.HashMap;

import javax.mail.Message;


public class UserTaskRequest {

	public enum Action {
		DONE, RVSP, INICIAR, ERROR, MULTI, RESET, FIN, AYUDA, SIGUIENTE, INFO
	}
	public UserTaskRequest() {
		super();
		this.hash = "";
		this.action = Action.ERROR;
		this.options = new HashMap<String, Object>();
		this.msg = null;
	}
	public UserTaskRequest(String hash, Action action, HashMap<String, Object> options, Message msg) {
		super();
		this.hash = hash;
		this.action = action;
		this.options = options;
		this.msg = msg;
	}
	public String					hash;
	public Action					action;
	public HashMap<String, Object>	options;
	public Message					msg;
}
