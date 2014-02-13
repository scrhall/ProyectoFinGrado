package isaFoundry.processEngine;


import java.util.HashMap;
import java.util.Map;


public class UserTaskRequest {

	public enum Action {
		DONE, RVSP,
	}

	public String					idProcces;
	public String					idTask;
	public Action					action;
	public HashMap<String, Object>	options;
}
