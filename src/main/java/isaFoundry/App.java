package isaFoundry;

import isaFoundry.core.Core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
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
       Core core=new Core();
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            //TODO:Comprobar tareas pendientes en el motor de activiti
        	//TODO:Comprobar emails y formularios para realizar tareas pendientes
        	
          }
        }, 0, 5, TimeUnit.SECONDS);      
             
                  
    }
}
