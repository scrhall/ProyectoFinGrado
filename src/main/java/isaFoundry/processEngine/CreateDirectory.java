package isaFoundry.processEngine;



import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDirectory implements ExecutionListener {

	private static Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	public void notify(DelegateExecution execution) throws Exception {
		String p= (String)execution.getVariable("ProjectName");	
		Log.info("Creando el directorio del proyecto: "+p);
		Folder f=Core.newFolder(p);
		Log.info("Carpeta Creada");
		String path=f.getPath();
		Log.info("Directorio creado: "+path);
		execution.setVariable("ProjectPath", path);
		
	}
}