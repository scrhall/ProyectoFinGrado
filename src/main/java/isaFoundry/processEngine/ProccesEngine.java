package isaFoundry.processEngine;


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

	private Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	private ProcessEngine	processEngine;

	public ProccesEngine() {
		this.processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		this.loadAllDefinitions();
		// ProccesEngine.startProces("myProcess");
	}

	public static int calculeHash(String a, String b) {
		return (a + b).hashCode();
	}

	public boolean doTask(UserTaskRequest t) {
		boolean res=false;
		TaskService taskService = this.processEngine.getTaskService();
		switch (t.action) {
			case DONE:
				List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
				for (Task task : tasks) {
					String dk = task.getTaskDefinitionKey();
					String pi = task.getProcessInstanceId();
					String hash = Integer.toString(calculeHash(dk , pi));
					if (hash.equals(t.hash)) {
						taskService.complete(task.getId() , t.options);
						this.Log.info("Task: " + task.getName() + " Completada, options: " + t.options.toString());
						res = true;
					}
				}
				break;
			case RVSP:
				break;
			default:
				return false;
		}
		return res;
	}

	public void startProces(String procesKey) {
		Map<String, Object> variables = new HashMap<String, Object>();
		RuntimeService runtimeService = this.processEngine.getRuntimeService();
		runtimeService.startProcessInstanceByKey(procesKey , variables);
		// Verificamos que se ha empezado la nueva instancia del proceso
		this.Log.info("Nunero de instancias: " + runtimeService.createProcessInstanceQuery().count());
	}

	public void startProces(String procesKey, Map<String, Object> var) {
		RuntimeService runtimeService = this.processEngine.getRuntimeService();
		runtimeService.startProcessInstanceByKey(procesKey , var);
		// Verificamos que se ha empezado la nueva instancia del proceso
		this.Log.info("Nunero de instancias: " + runtimeService.createProcessInstanceQuery().count());
	}

	private void loadAllDefinitions() {
		// TODO: mirar todos los bpmn en diagrams y cargarlos todos
		/*
		 * RepositoryService repositoryService =
		 * this.processEngine.getRepositoryService();
		 * final Collection<String> list =
		 * getResources(Pattern.compile(".+\\.bpmn"));
		 * for (final String name : list) {
		 * repositoryService.createDeployment().addInputStream(resourceName ,
		 * inputStream).addClasspathResource(name).deploy();
		 * this.Log.info("Number of process definitions: " +
		 * repositoryService.createProcessDefinitionQuery().count());
		 * }
		 */
		// Forma menos practica
		RepositoryService repositoryService = this.processEngine.getRepositoryService();
		// repositoryService.createDeployment().addClasspathResource("diagrams/FinalizacionProyecto.bpmn").deploy();
		repositoryService.createDeployment().addClasspathResource("diagrams/CreacionProyecto.bpmn")
				.addClasspathResource("diagrams/ConvenioMarco.bpmn").deploy();
		this.Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());
	}
}
