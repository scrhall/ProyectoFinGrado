package isaFoundry.processEngine;


import java.util.HashMap;


public class UserTaskRequest {

	public enum Action {
		DONE, RVSP,
	}

	public Integer					idTask;
	public Action					action;
	public HashMap<String, String>	options;
}
