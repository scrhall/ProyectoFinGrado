package isaFoundry.listeners;



import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;


import isaFoundry.core.*;
public class SendCompleteRequest implements ExecutionListener{

	public void notify(DelegateExecution arg0) throws Exception {
		// TODO Auto-generated method stub
		String linkRequest=Core.urlDoc((String)arg0.getVariable("RequestPath"));
		String to = (String)arg0.getVariable("DP");
		List<String> tos = new ArrayList<String>();
		tos.add(to);
		String subject = "Rellene la solicitud de Proyecto";
		String body = "A continuacion se adjunta un enlace con el que rellenar la "
				+ "solicitud de proyecto, un formulario para que introduzca los datos"
				+ "de la empresa y el correo enviado por la organizacion interesada."
				+ ""
				+ "Muchas gracias"
				+ "Link de la plantilla de solicitud de proyecto:"
				+ linkRequest
				+ "Link del formulario de empresa:"
				+ arg0.getVariable("CompanyForm")
				+ "El mensaje de la empresa interesada es:"
				+ (String)arg0.getVariable("CompanyMessage");
		Core.sendEmail(subject, body, tos);
	}

}
