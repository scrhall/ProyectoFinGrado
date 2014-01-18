package IsaFoundry.IsaFoundry_core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Core {
	/**
	 * Realiza el envio de un correo mediante la informacion proporcionada en xml.
	 * @param templatePath incluye toda la informacion del correo a enviar.
	 */
	public void sendEmail(String templatePath) {
		Document doc=readXmlFromUrl(templatePath);//Document doc=readXmlFromFile(templatePath)
			
		
	}
	/**
	 * Lee un xml desde una url.
	 * @param path url.
	 * @return
	 */
	public Document readXmlFromUrl(String path){
		try {
			URL url;
			String line;
			String xml="";
			url = new URL(path);
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = br.readLine()) != null){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Lee un xml desde un archivo.
	 * @param path
	 * @return
	 */
	public Document readXmlFromFile(String path){
		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();				
			Document doc;				
			doc = db.parse(new File(path));
			doc.getDocumentElement().normalize();
			return doc;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Copia un documento desde una ruta a otra.
	 * @param sourcePath 		origen.
	 * @param destinationPath 	destino.
	 */
	public void copyDoc(String sourcePath, String destinationPath) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Recupera la url para la edicion del documento en linea.
	 * @param doc
	 * @return
	 */
	public String urlDocGD(String doc) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Recupera la url para la obtencion del documento en formato pdf.
	 * @param doc
	 * @return
	 */
	public String urlDocPdf(String doc) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Recupera la url del documento.
	 * @param doc
	 * @return
	 */
	public String urlDoc(String doc) {
		// TODO Auto-generated method stub
		return null;
	}
}
