package isaFoundry.core;


import isaFoundry.contentManager.ContentManagerService;
import isaFoundry.core.UserTaskRequest.Action;
import isaFoundry.email.EmailService;
import isaFoundry.processEngine.ProcessEngineService;

import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Core {

	private static Logger					Log	= LoggerFactory.getLogger(Core.class);
	private static ContentManagerService	cManager;
	private static ProcessEngineService		pEngine;
	private static EmailService				eService;
	private static Core						core;

	public Core() {
		Log.info("Iniciando Core...");
		Core.core = this;
	}

	/**
	 * Copia un documento desde una ruta a otra.
	 * 
	 * @param filePath
	 *            Ruta completa del archivo
	 * @param destinationPath
	 *            Carpeta destino
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
		if (lt.size() == 0) {
			Log.info("No hay tareas pendientes.");
		} else {
			Log.info("Realizando " + lt.size() + " tareas pendientes.");
		}
		for (UserTaskRequest t : lt) {
			switch (t.action) {
				case INICIAR:
					Log.info("Comando 'INICIAR' detectado.");
					Core.startProcces(t.hash , t.options);
					break;
				case RESET:
					Log.info("Comando 'RESET' detectado.");
					Log.info("Se reiniciara la aplicacion en "+t.hash+ " Milisegundos.");
					try {
						Thread.sleep(tryParseInt(t.hash));
					} catch (InterruptedException e) {
						Log.error("Error: no se pudo reiniciar la aplicacion. \n" + e.getMessage());
						e.printStackTrace();
					}
					Log.info("Reiniciando Core...");
					Core.core.start();
					break;
				case ERROR:
					errorToResend(t , t.hash);
					break;
				case FIN:
					pEngine.endProcess(t.hash);
					break;
				case AYUDA:
					eService.reply(t.msg , help());
					break;
				case SIGUIENTE:					
						pEngine.doTask(new UserTaskRequest(t.hash,Action.DONE,t.options,t.msg));
						break;
				case INFO:
					eService.reply(t.msg , pEngine.info());
					break;
				default:
					if (!pEngine.doTask(t)) {
						errorToResend(
								t ,
								"No se encontro la tarea que coincida con el hash: "
										+ t.hash
										+ ", compruebue que incluye la utima etiqueta del mensaje del sistema al final de su correo con el siguiente formato: <--'Cadena de texto':'Codigo Numerico'-->");
					}
			}
		}
	}

	private static String help() {
		return " <h3><a name='_Toc388882396'>Comandos</a><o:p></o:p></h3>  <p class=MsoNormal>La estructura de los comandos que debemos enviar a la aplicación siempre es la misma, es muy genérica y servirá tanto para los comandos desarrollados como para futuros comandos sin ningún problema.<o:p></o:p></p>  <p class=MsoNormal>La estructura genérica sería la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:56.25pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:56.25pt'>   <p class=Codigo style='text-align:justify'>&lt;--<o:p></o:p></p>   <p class=Codigo style='text-align:justify'>NombreParametro1:Parametro1<o:p></o:p></p>   <p class=Codigo style='text-align:justify'>…<o:p></o:p></p>   <p class=Codigo style='text-align:justify'>NombreParametroN:ParametroN<o:p></o:p></p>   <p class=Codigo style='text-align:justify'>--&gt;<o:p></o:p></p>   <p class=Codigo style='text-align:justify'>&lt;--NombreComando:ParametroPrincipal--&gt;<o:p></o:p></p>   </td>  </tr> </table>  <h4>Comando INICIAR<o:p></o:p></h4>  <p class=MsoNormal>El comando INICIAR inicia un proceso en la aplicación.<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:56.25pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:56.25pt'>   <p class=Codigo>&lt;--<o:p></o:p></p>   <p class=Codigo>NombreParametro1:Parametro1<o:p></o:p></p>   <p class=Codigo>…<o:p></o:p></p>   <p class=Codigo>NombreParametroN:ParametroN<o:p></o:p></p>   <p class=Codigo>--&gt;<o:p></o:p></p>   <p class=Codigo>&lt;--INICIAR:NombreProceso--&gt;<o:p></o:p></p>   </td>  </tr> </table>  <p class=MsoNormal><span style='background:white'><br> Para cada proceso se necesitarán diferentes parámetros, dependiendo de las variables que vayan a intervenir en el proceso desarrollado.<span style='mso-spacerun:yes'>  </span><o:p></o:p></span></p>  <p class=MsoNormal><span style='background:white'>A continuación se indica la estructura de los parámetros según los diferentes procesos que han sido implementados:</span><o:p></o:p></p>  <h5><span style='background:white;mso-ansi-language:ES'>Proceso Creacion de Proyecto:</span><span style='mso-ansi-language:ES'><o:p></o:p></span></h5>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:56.25pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:56.25pt'>   <p class=Codigo>&lt;--<o:p></o:p></p>   <p class=Codigo>DPMail:Correo del DP<o:p></o:p></p>   <p class=Codigo>DETMail:Correo del DET<o:p></o:p></p>   <p class=Codigo>GerenciaMail:Correo de la Gerencia<o:p></o:p></p>   <p class=Codigo>RGFMail:Correo del RGF<o:p></o:p></p>   <p class=Codigo>ProjectName:Nombre del proyecto<o:p></o:p></p>   <p class=Codigo>--&gt;<o:p></o:p></p>   <p class=Codigo>&lt;--INICIAR:CreacionProyecto--&gt;<o:p></o:p></p>   </td>  </tr> </table>  <h5><span lang=EN-US style='background:white'>Proceso Reunion:</span><span lang=EN-US><o:p></o:p></span></h5>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt'>   <p class=Codigo>&lt;--<o:p></o:p></p>   <p class=Codigo>RGETMail:Correo del &nbsp;RGET<o:p></o:p></p>   <p class=Codigo>UGIDIET:Correos de los miembros de UGIDIET separado por comas<o:p></o:p></p>   <p class=Codigo>ProjectName: Nombre del proyecto<o:p></o:p></p>   <p class=Codigo>Reunion: Número de la reunión<o:p></o:p></p>   <p class=Codigo>--&gt;<o:p></o:p></p>   <p class=Codigo>&lt;--INICIAR:Reuniones--&gt;<o:p></o:p></p>   </td>  </tr> </table>  <h4>Comando FIN<o:p></o:p></h4>  <p class=MsoNormal>El comando FIN fuerza la eliminación de un proceso determinado.<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:3.7pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:3.7pt'>   <p class=Codigo><span style='background:white'>&lt;--FIN:Id del proceso--&gt;<span   style='mso-tab-count:1'>        </span></span><o:p></o:p></p>   </td>  </tr> </table>  <h4>Comando RESET<o:p></o:p></h4>  <p class=MsoNormal>El comando RESET fuerza el reinicio de la aplicación en un tiempo determinado.<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:6.1pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:6.1pt'>   <p class=Codigo><span style='background:white'>&lt;--RESET:Tiempo en   milisegundos--&gt;</span><o:p></o:p></p>   </td>  </tr> </table>  <h4>Comando AYUDA<o:p></o:p></h4>  <p class=MsoNormal>El comando AYUDA envía información de ayuda general o específica para el uso de la aplicación .<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:14.1pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:14.1pt'>   <p class=Codigo><span style='mso-bidi-font-size:9.0pt;font-family:Consolas;   color:black;background:white'>&lt;--AYUDA:Nombre del Comando--&gt;</span><o:p></o:p></p>   </td>  </tr> </table>  <p class=MsoNormal><o:p>&nbsp;</o:p></p>  <p class=MsoNormal><span style='background:white'>También se puede indicar como nombre de comando “GENERAL”, que enviara ayuda general de la aplicación.</span><o:p></o:p></p>  <h4>Comando SIGUIENTE<o:p></o:p></h4>  <p class=MsoNormal>El comando SIGUIENTE &nbsp;fuerza la finalización de una tarea cualquiera con los parámetros que se quiera.<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:44.25pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:44.25pt'>   <p class=Codigo><span style='background:white'>&lt;--</span><o:p></o:p></p>   <p class=Codigo><span style='color:#660066;background:white'>NombreParametro1</span><span   style='color:#666600;background:white'>:</span><span style='color:#660066;   background:white'>Parametro1</span><o:p></o:p></p>   <p class=Codigo><span style='color:#660066;background:white'>…</span><o:p></o:p></p>   <p class=Codigo><span style='color:#660066;background:white'>NombreParametroN</span><span   style='color:#666600;background:white'>:</span><span style='color:#660066;   background:white'>ParametroN</span><o:p></o:p></p>   <p class=Codigo><span style='color:#666600;background:white'>--&gt;</span><o:p></o:p></p>   <p class=Codigo><span style='background:white'>&lt;SIGUIENTE:Hash de la   tarea--&gt;</span><o:p></o:p></p>   </td>  </tr> </table>  <p class=MsoNormal><o:p>&nbsp;</o:p></p>  <p class=MsoNormal><span style='font-size:10.0pt;line-height:107%;font-family: 'Arial','sans-serif';color:#222222;background:white'>El hash de las tareas puede ser encontrado al final de los correos enviados por la aplicación o mediante el comando INFO.</span><o:p></o:p></p>  <h4>Comando INFO<o:p></o:p></h4>  <p class=MsoNormal>El comando INFO envía información sobre los procesos y tareas.<o:p></o:p></p>  <p class=MsoNormal>La estructura del comando debe de ser la siguiente:<o:p></o:p></p>  <table class=MsoNormalTable border=0 cellspacing=0 cellpadding=0 width=565  style='width:423.45pt;border-collapse:collapse;mso-yfti-tbllook:1184'>  <tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes;   height:8.65pt'>   <td width=565 valign=top style='width:423.45pt;border:solid black 1.0pt;   mso-border-alt:solid black .75pt;padding:5.25pt 5.25pt 5.25pt 5.25pt;   height:8.65pt'>   <p class=Codigo><span style='background:white'>&lt;INFO:GENERAL--&gt;</span><o:p></o:p></p>   </td>  </tr> </table>  ";
	}

	/**
	 * Crea un nuevo directorio en el repositorio
	 * 
	 * @param path
	 *            Ruta del directorio
	 */
	public static String newFolder(String path) {
		return cManager.newFolder(path).getPath();
	}

	/**
	 * Realiza el envio de un correo mediante la informacion proporcionada en
	 * xml.
	 * 
	 * @param templatePath
	 *            Incluye toda la informacion del correo a enviar.
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
		pEngine.startProcess(procesKey , var);
	}

	/**
	 * Crea un documento pdf mediante una carpeta intermedia con regla de
	 * conversión asociada
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Carpeta origen del documento
	 * @param targetPath
	 *            Carpeta destino del documento pdf
	 * @param converterPath
	 *            Ruta de la carpeta de transformación
	 */
	public static void toPDF(String fileName, String sourcePath, String targetPath, String converterPath) {
		cManager.toPDF(fileName , sourcePath , targetPath , converterPath);
	}

	/**
	 * Recupera la url del documento.
	 * 
	 * @param doc
	 *            Ruta al documento
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

	/**
	 * Metodo para notificar a un usuario que su mensaje no pudo ser procesado.
	 * 
	 * @param t
	 */
	private static void errorToResend(UserTaskRequest t) {
		errorToResend(t , "Se ha detectado un problema al procesar su respuesta, compruebe los datos enviados y intentelo de nuevo.");
	}

	private static void errorToResend(UserTaskRequest t, String msg) {
		eService.reply(t.msg , msg);
	}

	static int tryParseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public void start() {
		Log.info("Conectando a los Servicios de Core...");
		cManager = new ContentManagerService();
		pEngine = new ProcessEngineService();
		eService = new EmailService();
		eService.connect();
	}
}
