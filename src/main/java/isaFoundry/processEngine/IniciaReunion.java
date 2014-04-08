package isaFoundry.processEngine;


import java.util.HashMap;
import java.util.Map;

import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.eclipse.jetty.util.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IniciaReunion implements ExecutionListener {

	private static Logger	Log	= LoggerFactory.getLogger(ProccesEngine.class);

	public void notify(DelegateExecution execution) throws Exception {
		// Preparamos las variables necesarias
		Log.info("Inicando el listener iniciaReunion");
		String RGET = (String) execution.getVariable("RGETMail");
		//String[] UGIDIET =(String[]) execution.getVariable("UGIDIET");
		String ProjectName = (String) execution.getVariable("aux");
		Integer r =(Integer) execution.getVariable("next");
		
		ProjectName=ProjectName+r.toString();
		String Reunion=r.toString();
		Log.info("creando el mapa de variables");
		Map<String,Object> var = new HashMap<String, Object>();
		var.put("RGETMail", RGET);
		var.put("UGIDIET",execution.getVariable("UGIDIET"));
		var.put("ProjectName",ProjectName);
		var.put("Reunion",Reunion);
		Log.info("Mapa de variables: " +var.toString());
		// Copiamos y guardamos el path del documento
		Core.startProcces("Reuniones",var);
	}
}