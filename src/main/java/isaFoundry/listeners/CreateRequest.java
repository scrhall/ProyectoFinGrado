package isaFoundry.listeners;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import isaFoundry.core.*;

public class CreateRequest implements ExecutionListener{

	public void notify(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub		
		execution.setVariable("RequestPath", "Raiz"+(String)execution.getProcessDefinitionId()+"Solicitud");		
		Core.copyDoc("RutaPlantilla/SolicitudProyecto", (String)execution.getVariable("RequestPath"));
		
	}
	
	
	
}
