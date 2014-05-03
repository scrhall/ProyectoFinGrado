package isaFoundry.processEngine;


import isaFoundry.core.UserTaskRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProccesEngine {

	private static Logger	Log	= LoggerFactory.getLogger(ProccesEngine.class);
	private ProcessEngine	processEngine;

	/**
	 * Incializa el motor de proceso y carga todos los procesos necesarios.
	 */
	public ProccesEngine() {
		Log.info("Iniciando el motor de proceso...");
		this.processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
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
		TaskService taskService = this.processEngine.getTaskService();
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
						List<String> tos = (List<String>) taskService.getVariable(task.getId() , "tos");
						List<HashMap<String, Object>> tosResponse = (List<HashMap<String, Object>>) taskService.getVariable(task.getId() , "tosResponse");
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
							Log.info("tosResponse actualizada: "+tosResponse);
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
	 * @param procesKey
	 *            Nombre del proceso a iniciar.
	 * @param var
	 *            Parametros de entrada en el proceso.
	 */
	public void startProcess(String procesKey, Map<String, Object> var) {
		Log.info("Iniciando el  proceso '" + procesKey + "'");
		RuntimeService runtimeService = this.processEngine.getRuntimeService();
		runtimeService.startProcessInstanceByKey(procesKey , var);
	}

	/**
	 * Carga todas las definiciones de los procesos.
	 */
	private void loadAllDefinitions() {
		Log.info("Cargando las definiciones de los procesos...");
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		// repositoryService.createDeployment().addClasspathResource("diagrams/FinalizacionProyecto.bpmn").deploy();
		repositoryService.createDeployment().addClasspathResource("diagrams/CreacionProyecto.bpmn")
				.addClasspathResource("diagrams/ConvenioMarco.bpmn").addClasspathResource("diagrams/Reuniones2.bpmn").addClasspathResource("diagrams/diagramaPrueba.bpmn").deploy();
		ProccesEngine.Log.info("Numero de definiciones cargadas: " + repositoryService.createProcessDefinitionQuery().count());
	}
}
