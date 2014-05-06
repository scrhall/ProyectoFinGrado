package isaFoundry.core;


import java.util.HashMap;

import javax.mail.Message;


public class UserTaskRequest {

	public enum Action {
		DONE, RVSP, INICIAR, ERROR, MULTI, RESET, FIN
	}

	public String					hash;
	public Action					action;
	public HashMap<String, Object>	options;
	public Message					msg;
}
