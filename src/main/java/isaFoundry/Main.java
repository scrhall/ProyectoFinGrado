package isaFoundry;


import isaFoundry.core.Core;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 * 
 */
public class Main {

	public static void main(String[] args) throws Exception {
		Logger Log = LoggerFactory.getLogger(Main.class);
		Log.info("Ejecutando");
		Core core = new Core();
		core.start();
		/*
		// Core.startProces("ConvenioMarco", new HashMap<String,Object>());
		Map<String, Object> var = new HashMap<String, Object>();
		/*
		 * var.put("tos" , "scrhall@scrhall.com");
		 * var.put("subject" , "Prueba de envio de correo");
		 * var.put("body" , "Prueba body");
		
		
		
		var.put("DPMail" , "scrhall@scrhall.com");
		var.put("DETMail" , "scrhall@scrhall.com");
		var.put("GerenciaMail" , "scrhall@scrhall.com");
		var.put("RGFMail" , "scrhall@scrhall.com");
		var.put("ProjectName" , "ProyectoPrueba");
		var.put("TemplateDirectory" , "/Plantillas/CreacionProyecto");
		var.put("Doc" , "Solicitud.docx");
		
		
		var.put("DPMail" , "jualopver@gmail.com");
		//var.put("DETMail" , "jualopver@gmail.com");
		var.put("GerenciaMail" , "jualopver@gmail.com");
		var.put("RGFMail" , "jualopver@gmail.com");
		var.put("ProjectName" , "ProyectoPrueba");
		var.put("TemplateDirectory" , "/Plantillas/CreacionProyecto");
		var.put("Doc" , "Solicitud.docx");
		
		Core.startProcces("CreacionProyecto" , var);
		*/
	}
}
