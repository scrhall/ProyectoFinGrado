package isaFoundry.processEngine;


import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateDirectory implements ExecutionListener {

	private static Logger	Log	= LoggerFactory.getLogger(ProccesEngine.class);

	public void notify(DelegateExecution execution) throws Exception {
		String p = "/Procesos/" + (String) execution.getVariable("ProjectName");
		Log.info("Creando el directorio del proyecto: " + p);
		String path = Core.newFolder(p);
		Log.info("Carpeta Creada");
		Log.info("Directorio creado: " + path);
		execution.setVariable("ProjectPath" , path);
	}
}