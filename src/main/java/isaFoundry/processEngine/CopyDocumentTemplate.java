package isaFoundry.processEngine;


import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CopyDocumentTemplate implements ExecutionListener {

	private static Logger	Log	= LoggerFactory.getLogger(ProccesEngine.class);

	public void notify(DelegateExecution execution) throws Exception {
		// Preparamos las variables necesarias
		String td = (String) execution.getVariable("TemplateDirectory");
		String dest = (String) execution.getVariable("ProjectPath");
		String doc = (String) execution.getVariable("Doc");
		String source = td + "/" + doc;
		// dest=dest+"/"+doc;
		// Copiamos y guardamos el path del documento
		Log.info("Copiando Documento de: " + source + " a: " + dest);
		Core.copyDoc(source , dest);
		execution.setVariable("Path" + doc , dest);
		Log.info("Copia finalizada de: " + source + " a: " + dest);
		// Obtenemos la URL y la guardamos
		dest = dest + "/" + doc;
		Log.info("Creando variable URL" + doc);
		String URLDoc = Core.urlDocOnlineEdit(dest);
		Log.info("Creando variable URL" + doc + " con valor: " + URLDoc);
		String[] n=doc.split("\\.");
		execution.setVariable("URL" + n[0] , URLDoc);
		// Podriamos añadir PDF
	}
}
