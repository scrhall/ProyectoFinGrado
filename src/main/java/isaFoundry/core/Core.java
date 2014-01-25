package isaFoundry.core;


import isaFoundry.contentManager.ContentManager;
import isaFoundry.email.EmailService;
import isaFoundry.processEngine.ProccesEngine;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Core {

	private Logger					Log	= LoggerFactory.getLogger(Core.class);
	private static ContentManager	cManager;
	private static ProccesEngine	pEngine;
	private static EmailService		eService;

	/**
	 * Copia un documento desde una ruta a otra.
	 * 
	 * @param sourcePath
	 *            origen.
	 * @param destinationPath
	 *            destino.
	 */
	public static void copyDoc(String sourcePath, String destinationPath) {
		cManager.copyDoc(sourcePath , destinationPath);
	}

	/**
	 * Realiza el envio de un correo mediante la informacion proporcionada en
	 * xml.
	 * 
	 * @param templatePath
	 *            incluye toda la informacion del correo a enviar.
	 */
	public static void sendEmail(String subject, String body, List<String> tos) {
		eService.SendEmail(subject , body , tos);
	}

	/**
	 * Recupera la url del documento.
	 * 
	 * @param doc
	 * @return
	 */
	public static String urlDoc(String doc) {
		// TODO: hay que comprobar realmente que paramtro o valor vamos a tener
		// cManager.getDocumentURL(document);
		return null;
	}

	public Core() {
		this.Log.info("Iniciando Motor de proceso");
		Core.pEngine = new ProccesEngine();
		// this.Log.info("Iniciando Gestor documental");
		// this.cManager = new ContentManager();
	}

	public void run() {
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {

			public void run() {
				Core.this.Log.info("loop ejecutandose...");
				// TODO:Comprobar tareas pendientes en el motor de activiti
				// TODO:Comprobar emails y formularios para realizar tareas
				// pendientes
			}
		} , 0 , 5 , TimeUnit.SECONDS);
	}

	/**
	 * Recupera la url para la edicion del documento en linea.
	 * 
	 * @param doc
	 * @return
	 */
	public String urlDocOnlineEdit(String doc) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Recupera la url para la obtencion del documento en formato pdf.
	 * 
	 * @param doc
	 * @return
	 */
	public String urlDocPdf(String doc) {
		// TODO: falta por hacer conincidir los parametros que tendremos por los
		// que necesita la funcion
		// cManager.toPDF(fileName , filePath , targetPath);
		return null;
	}
}
