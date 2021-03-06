package isaFoundry.processEngine;


import isaFoundry.contentManager.ContentManagerService;
import isaFoundry.core.UserTaskRequest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessEngineService {

	private static Logger			Log	= LoggerFactory.getLogger(ProcessEngineService.class);
	private static ProcessEngine	processEngine;

	/**
	 * Incializa el motor de proceso y carga todos los procesos necesarios.
	 * 
	 * @throws Exception
	 */
	public ProcessEngineService() {
		Log.info("Iniciando el motor de proceso...");
		if (processEngine != null) {
			try {
				Class.forName("org.h2.Driver");
				Connection con = DriverManager.getConnection("jdbc:h2:mem:activiti" , "sa" , "");
				Statement stmt = con.createStatement();
				stmt.executeUpdate("SHUTDOWN");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		this.loadAllDefinitions();
	}

	/**
	 * Calcula un hash para dos string dados.
	 * 
	 * @param a
	 * @param b
	 * @return Hash
	 */
	public static int calculeHash(String a, String b) {
		return (a + b).hashCode();
	}

	/**
	 * Realiza una tarea.
	 * 
	 * @param t
	 * @return
	 */
	public boolean doTask(UserTaskRequest t) {
		Log.info("Iniciando tarea...");
		boolean res = false;
		TaskService taskService = ProcessEngineService.processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().active().list();
		switch (t.action) {
			case DONE:
				for (Task task : tasks) {
					String dk = task.getTaskDefinitionKey();
					String pi = task.getProcessInstanceId();
					String hash = Integer.toString(calculeHash(dk , pi));
					if (hash.equals(t.hash)) {
						taskService.complete(task.getId() , t.options);
						Log.info("Task: " + task.getName() + " Completada, options: " + t.options.toString());
						res = true;
						break;
					}
				}
				break;
			case MULTI:
				for (Task task : tasks) {
					String dk = task.getTaskDefinitionKey();
					String pi = task.getProcessInstanceId();
					String hash = Integer.toString(calculeHash(dk , pi));
					if (hash.equals(t.hash)) {
						List<String> tos = new ArrayList<String>();
						if (taskService.getVariable(task.getId() , "tos") instanceof String) {
							tos.add((String) taskService.getVariable(task.getId() , "tos"));
						} else {
							if (taskService.getVariable(task.getId() , "tos") instanceof List<?>) {
								tos = (List<String>) taskService.getVariable(task.getId() , "tos");
							} else {
								break;
							}
						}
						List<HashMap<String, Object>> tosResponse = (List<HashMap<String, Object>>) taskService.getVariable(task.getId() ,
								"tosResponse");
						if (tosResponse == null) {
							tosResponse = new ArrayList<HashMap<String, Object>>();
						}
						String[] auxFrom = t.options.get("From").toString().split("<|>");
						if ((auxFrom.length > 1) && tos.contains(auxFrom[1])) {
							for (HashMap<String, Object> list : tosResponse) {
								if (list.get("From").equals(t.options.get("From"))) {
									tosResponse.remove(list);
									break;
								}
							}
							tosResponse.add(t.options);
							Log.info("tosResponse actualizada: " + tosResponse);
							taskService.setVariable(task.getId() , "tosResponse" , tosResponse);
							Log.info("Task: '" + task.getName() + "' Actualizada.");
							if (tos.size() == tosResponse.size()) {
								taskService.complete(task.getId());
								Log.info("Task: '" + task.getName() + "' Completada.");
							}
							res = true;
						}
						break;
					}
				}
				break;
			default:
				return false;
		}
		return res;
	}

	public void endProcess(String processId) {
		Log.info("Eliminando el  proceso '" + processId + "'");
		RuntimeService runtimeService = ProcessEngineService.processEngine.getRuntimeService();
		runtimeService.deleteProcessInstance(processId , null);
	}

	/**
	 * Inicia un proceso en el motor de procesos.
	 * 
	 * @param procesKey
	 *            Nombre del proceso a iniciar.
	 */
	public void startProcess(String procesKey) {
		this.startProcess(procesKey , new HashMap<String, Object>());
	}

	/**
	 * Inicia un proceso en el motor de procesos.
	 * 
	 * @param processKey
	 *            Nombre del proceso a iniciar.
	 * @param var
	 *            Parametros de entrada en el proceso.
	 */
	public void startProcess(String processKey, Map<String, Object> var) {
		Log.info("Iniciando el  proceso '" + processKey + "'");
		RuntimeService runtimeService = ProcessEngineService.processEngine.getRuntimeService();
		runtimeService.startProcessInstanceByKey(processKey , var);
	}

	/**
	 * Carga todas las definiciones de los procesos.
	 */
	private void loadAllDefinitions() {
		Properties	properties	= new Properties();
		
		Log.info("Cargando las definiciones de los procesos...");
		RepositoryService repositoryService = ProcessEngineService.processEngine.getRepositoryService();
		try {
			properties.load(ProcessEngineService.class.getResourceAsStream("/config/processEngine.properties"));
			String[] diagrams=properties.getProperty("DIAGRAMS").split(",");
			for (String string : diagrams) {
				repositoryService.createDeployment().addClasspathResource("diagrams/"+string+".bpmn").deploy();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ProcessEngineService.Log.info("Numero de definiciones cargadas: " + repositoryService.createProcessDefinitionQuery().count());
	}

	public String info() {
		TaskService taskService = ProcessEngineService.processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().active().list();
		RepositoryService repositoryService = ProcessEngineService.processEngine.getRepositoryService();
		String res="";
		res+="<br>Numero de definiciones cargadas: " + repositoryService.createProcessDefinitionQuery().count();
		res+="<br>";
		res+="<br>Tareas activas:";
		for (Task task : tasks) {
			String dk = task.getTaskDefinitionKey();
			String pi = task.getProcessInstanceId();
			String hash = Integer.toString(calculeHash(dk , pi));
			res+="<br>Id Proceso:"+task.getProcessDefinitionId()+" Tarea:"+task.getName()+" Hash: "+hash;
		}
		
		return res;
	}

}
