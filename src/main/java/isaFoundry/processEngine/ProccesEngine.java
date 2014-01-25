package isaFoundry.processEngine;


import isaFoundry.core.Core;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProccesEngine {

	public class CompleteRequest implements ExecutionListener {

		public void notify(DelegateExecution arg0) throws Exception {
			// TODO Auto-generated method stub
		}
	}

	public class CreateRequest implements ExecutionListener {

		public void notify(DelegateExecution execution) throws Exception {
			execution.setVariable("RequestPath" , "Raiz" + execution.getProcessDefinitionId() + "Solicitud");
			Core.copyDoc("RutaPlantilla/SolicitudProyecto" , (String) execution.getVariable("RequestPath"));
		}
	}

	public class SendCompleteRequest implements ExecutionListener {

		public void notify(DelegateExecution arg0) throws Exception {
			String linkRequest = Core.urlDoc((String) arg0.getVariable("RequestPath"));
			String to = (String) arg0.getVariable("DP");
			List<String> tos = new ArrayList<String>();
			tos.add(to);
			// Esta parte o se carga de alfresco o directamente del BPMN
			String subject = "Rellene la solicitud de Proyecto";
			String body = "A continuacion se adjunta un enlace con el que rellenar la "
					+ "solicitud de proyecto, un formulario para que introduzca los datos"
					+ "de la empresa y el correo enviado por la organizacion interesada." + "" + "Muchas gracias"
					+ "Link de la plantilla de solicitud de proyecto:" + linkRequest + "Link del formulario de empresa:"
					+ arg0.getVariable("CompanyForm") + "El mensaje de la empresa interesada es:" + (String) arg0.getVariable("CompanyMessage");
			Core.sendEmail(subject , body , tos);
		}
	}

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
		repositoryService.createDeployment().addClasspathResource("isaFoundry/diagrams/CreacionProyecto.bpmn").deploy();
		this.Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());
	}
}
