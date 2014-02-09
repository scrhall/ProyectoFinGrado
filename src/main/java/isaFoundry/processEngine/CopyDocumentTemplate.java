package isaFoundry.processEngine;

import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyDocumentTemplate implements ExecutionListener{

	private static Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	public void notify(DelegateExecution execution) throws Exception {
		//Preparamos las variables necesarias
		String td= (String)execution.getVariable("TemplateDirectory");
		String dest=(String) execution.getVariable("ProjectPath");
		String doc=(String) execution.getVariable("Doc");
		String source=td+"/"+doc;
		dest=dest+"/"+doc;
		
		//Copiamos y guardamos el path del documento
		Log.info("CopiandoDocumento de: "+source+" a: "+dest);
		Core.copyDoc(source, dest);
		execution.setVariable("Path"+doc, dest);
		
		//Obtenemos la URL y la guardamos
		String URLDoc=Core.urlDoc(dest);
		Log.info("Creando variable URL"+doc+" con valor: "+URLDoc);
		execution.setVariable("URL"+doc, URLDoc);
		
		//Podriamos añadir PDF 
		
		
	}
}
