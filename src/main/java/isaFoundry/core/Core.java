package isaFoundry.core;


import isaFoundry.contentManager.ContentManager;
import isaFoundry.email.EmailService;
import isaFoundry.processEngine.ProccesEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Core {

	private Logger			Log	= LoggerFactory.getLogger(Core.class);
	private static ContentManager	cManager;
	private static ProccesEngine	pEngine;
	private static EmailService	eService;

	public Core() {
		this.Log.info("Iniciando Motor de proceso");
		this.pEngine = new ProccesEngine();
		// this.Log.info("Iniciando Gestor documental");
		// this.cManager = new ContentManager();
	}

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
	 * Lee un xml desde un archivo.
	 * 
	 * @param path
	 * @return
	 */
	public Document readXmlFromFile(String path) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document doc;
			doc = db.parse(new File(path));
			doc.getDocumentElement().normalize();
			return doc;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Lee un xml desde una url.
	 * 
	 * @param path
	 *            url.
	 * @return
	 */
	public Document readXmlFromUrl(String path) {
		try {
			URL url;
			String line;
			String xml = "";
			url = new URL(path);
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = br.readLine()) != null) {
				xml = xml + line;
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			InputSource file = new InputSource();
			file.setCharacterStream(new StringReader(xml));
			Document doc;
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
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
	 * Realiza el envio de un correo mediante la informacion proporcionada en
	 * xml.
	 * 
	 * @param templatePath
	 *            incluye toda la informacion del correo a enviar.
	 */
	public static void sendEmail(String subject, String body, List<String> tos) {
		eService.SendEmail(subject, body, tos);
	}

	/**
	 * Recupera la url del documento.
	 * 
	 * @param doc
	 * @return
	 */
	public static String urlDoc(String doc) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}
}
