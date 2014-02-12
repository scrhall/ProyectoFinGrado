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
		Core.run();
		// Core.startProces("ConvenioMarco", new HashMap<String,Object>());
		Map<String, Object> var = new HashMap<String, Object>();
		var.put("tos" , "scrhall@scrhall.com");
		var.put("subject" , "Prueba de envio de correo");
		var.put("body" , "Prueba body");
		Core.startProcces("myProcess" , var);
		
		/*
		var.put("DP-Mail" , "jualopver@gmail.com");
		var.put("DET-Mail" , "jualopver@gmail.com");
		var.put("Gerencia-Mail" , "jualopver@gmail.com");
		var.put("RGF-Mail" , "jualopver@gmail.com");
		var.put("ProjectName" , "ProyectoPrueba");
		var.put("TemplateDirectory" , "/template");
		var.put("Doc" , "ProjectApplication");
		
		
		 * String webappDirLocation = "src/main/webapp/";
		 * // The port that we should run on can be set into an environment
		 * // variable
		 * // Look for that variable and default to 8080 if it isn't there.
		 * String webPort = System.getenv("PORT");
		 * if ((webPort == null) || webPort.isEmpty()) {
		 * webPort = "8080";
		 * }
		 * Server server = new Server(Integer.valueOf(webPort));
		 * WebAppContext root = new WebAppContext();
		 * root.setContextPath("/");
		 * root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
		 * root.setResourceBase(webappDirLocation);
		 * // Parent loader priority is a class loader setting that Jetty
		 * accepts.
		 * // By default Jetty will behave like most web containers in that it
		 * will
		 * // allow your application to replace non-server libraries that are
		 * part
		 * // of the
		 * // container. Setting parent loader priority to true changes this
		 * // behavior.
		 * // Read more here:
		 * // http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
		 * root.setParentLoaderPriority(true);
		 * server.setHandler(root);
		 * server.start();
		 * server.join();
		 */
	}
}
