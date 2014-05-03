package isaFoundry.processEngine;


import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CopyDocument implements ExecutionListener {

	private static Logger	Log	= LoggerFactory.getLogger(ProcessEngineService.class);

	public void notify(DelegateExecution execution) throws Exception {
		// Preparamos las variables necesarias
		String src = (String) execution.getVariable("src");
		String dest = (String) execution.getVariable("dest");
		String doc = (String) execution.getVariable("Doc");
		// Copiamos y guardamos el path del documento
		Log.info("CopiandoDocumento de: " + src + " a: " + dest);
		Core.copyDoc(src , dest, doc+".docx");
		execution.setVariable("Path" + doc , dest);
		// Obtenemos la URL y la guardamos
		String URLDoc = Core.urlDoc(dest+"/"+doc+".docx");
		Log.info("Creando variable URL" + doc + " con valor: " + URLDoc);
		execution.setVariable("URL" + doc , URLDoc);
		// Podriamos añadir PDF
	}
}