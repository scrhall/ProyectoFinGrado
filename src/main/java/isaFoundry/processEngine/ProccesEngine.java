package isaFoundry.processEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProccesEngine {

	private static Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	private static ProcessEngine	processEngine;

	public ProccesEngine() {
		this.processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		this.LoadAllDefinitions();
		//ProccesEngine.startProces("myProcess");
	}

	private void LoadAllDefinitions() {
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
		
		//repositoryService.createDeployment().addClasspathResource("diagrams/FinalizacionProyecto.bpmn").deploy();
		repositoryService.createDeployment().addClasspathResource("diagrams/diagramaPrueba.bpmn").deploy();
		
		this.Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());
	}
	
	public static void startProces(String procesKey){
		Map<String, Object> variables = new HashMap<String, Object>();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(procesKey, variables);
		      
		// Verificamos que se ha empezado la nueva instancia del proceso
		Log.info("Nunero de instancias: " + runtimeService.createProcessInstanceQuery().count());
		
	}

	public static void doTasks(List<UserTaskRequest> lt) {
		for (UserTaskRequest t:lt){
			doTask(t);
		}
	}
	
	public static void doTask(UserTaskRequest t){
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
		for (Task task : tasks) {
			if (task.getId()==t.getId()){
				taskService.complete(task.getId(), t.getOptions());
				Log.info("Task: " + task.getName()+" complete, options: "+t.getOptions().toString());
			}
			
			
		}
	}
}
