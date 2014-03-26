package isaFoundry.core;


import isaFoundry.contentManager.ContentManager;
import isaFoundry.email.EmailService;
import isaFoundry.processEngine.ProccesEngine;
import isaFoundry.processEngine.UserTaskRequest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Core {

	private static Logger			Log	= LoggerFactory.getLogger(Core.class);
	private static ContentManager	cManager;
	private static ProccesEngine	pEngine;
	private static EmailService		eService;

	public Core() {}

	/**
	 * Copia un documento desde una ruta a otra.
	 * 
	 * @param filePath
	 *            ruta completa del archivo
	 * @param destinationPath
	 *            carpeta destino
	 */
	public static void copyDoc(String filePath, String destinationPath, String name) {
		cManager.copyDoc(filePath , destinationPath , name);
	}

	/**
	 * Realiza las tareas suministradas en una lista, segun el tipo de tarea que
	 * sean
	 * 
	 * @param lt
	 */
	public static void doTasks(List<UserTaskRequest> lt) {
		for (UserTaskRequest t : lt) {
			switch (t.action) {
				case INICIAR:
					Core.startProcces(t.hash , t.options);
					break;
				default:
					if(!pEngine.doTask(t)) errorToResend(t);
			}
		}
	}

	private static void errorToResend(UserTaskRequest t) {
		eService.reply(t.msg,"Se ha detectado un problema al procesar su respuesta, compruebe los datos enviados y intentelo de nuevo.");
	}

	/**
	 * Crea un nuevo directorio en el repositorio
	 * 
	 * @param path
	 *            ruta del directorio
	 */
	public static String newFolder(String path) {
		return cManager.newFolder(path).getPath();
	}

	/**
	 * Realiza el envio de un correo mediante la informacion proporcionada en
	 * xml.
	 * 
	 * @param templatePath
	 *            incluye toda la informacion del correo a enviar.
	 */
	public static void sendEmail(String subject, String body, List<String> tos) {
		eService.sendEmail(subject , body , tos);
	}

	/**
	 * Inicia un proceso determinado en el motor de proceso
	 * 
	 * @param procesKey
	 * @param var
	 */
	public static void startProcces(String procesKey, Map<String, Object> var) {
		pEngine.startProces(procesKey , var);
	}

	/**
	 * Crea un documento pdf mediante una carpeta intermedia con regla de
	 * conversión asociada
	 * 
	 * @param fileName
	 *            nombre del documento
	 * @param sourcePath
	 *            carpeta origen del documento
	 * @param targetPath
	 *            carpeta destino del documento pdf
	 * @param converterPath
	 *            ruta de la carpeta de transformación
	 */
	public static void toPDF(String fileName, String sourcePath, String targetPath, String converterPath) {
		cManager.toPDF(fileName , sourcePath , targetPath , converterPath);
	}

	/**
	 * Recupera la url del documento.
	 * 
	 * @param doc
	 *            ruta al documento
	 * @return String que representa la url del documento en nuestro repositorio
	 */
	public static String urlDoc(String doc) {
		return cManager.getDocumentURL(doc);
	}

	/**
	 * Recupera la url para la edicion del documento en linea.
	 * 
	 * @param doc
	 * @return String que representa la url de acceso a edición mediante google
	 *         docs en nuestro repositorio
	 */
	public static String urlDocOnlineEdit(String doc) {
		return cManager.getOnlineEditURL(doc);
	}

	public void start() {
		cManager = new ContentManager();
		pEngine = new ProccesEngine();
		eService = new EmailService();
		eService.connect();
	}
}
