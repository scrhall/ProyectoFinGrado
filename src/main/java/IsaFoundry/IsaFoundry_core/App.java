package IsaFoundry.IsaFoundry_core;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Logger Log = LoggerFactory.getLogger(App.class);
        ProcessEngine processEngine=ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createDeployment()
          .addClasspathResource("FinalizacionProyecto.bpmn")
          .deploy();
              
        Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());            
                
                  
    }
}
