package isaFoundry.processEngine;


import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProccesEngine {

	private Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	private ProcessEngine	processEngine;

	public ProccesEngine() {
		this.processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
		this.LoadAllDefinitions();
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
		repositoryService.createDeployment().addClasspathResource("isaFoundry/diagrams/FinalizacionProyecto.bpmn").deploy();
		this.Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());
	}
}
